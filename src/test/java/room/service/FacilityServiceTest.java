package room.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import room.dto.request.FacilityCreateRequest;
import room.dto.request.FacilityUpdateRequest;
import room.dto.response.FacilityDetailResponse;
import room.entity.Facility;
import room.entity.enums.FacilityCategory;
import room.repository.FacilityRepository;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    void getsAllFacilitiesIncludingInactiveFacilities() {
        Facility activeFacility = facility(
                1L, "Meditation Room", FacilityCategory.WELLNESS, true
        );
        Facility inactiveFacility = facility(
                2L, "Old Lounge", FacilityCategory.FOOD, false
        );
        given(facilityRepository.findAll())
                .willReturn(List.of(activeFacility, inactiveFacility));

        List<FacilityDetailResponse> responses = facilityService.getAllFacilities();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(FacilityDetailResponse::active)
                .containsExactly(true, false);
    }

    @Test
    void getsFacilityDetail() {
        Facility facility = facility(1L, "Meditation Room", FacilityCategory.WELLNESS, true);
        given(facilityRepository.findById(1L)).willReturn(Optional.of(facility));

        FacilityDetailResponse response = facilityService.getFacility(1L);

        assertThat(response.facilityId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Meditation Room");
        assertThat(response.category()).isEqualTo(FacilityCategory.WELLNESS);
        assertThat(response.active()).isTrue();
    }

    @Test
    void returnsNotFoundWhenGettingMissingFacility() {
        given(facilityRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.getFacility(99L))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value())
                                .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void createsFacilityWithActiveDefaultingToTrue() {
        FacilityCreateRequest request = new FacilityCreateRequest(
                "Meditation Room",
                FacilityCategory.WELLNESS,
                "A quiet shared space",
                "https://example.com/meditation-room.jpg",
                null
        );
        given(facilityRepository.existsByName(request.name())).willReturn(false);
        given(facilityRepository.save(any(Facility.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        FacilityDetailResponse response = facilityService.createFacility(request);

        assertThat(response.name()).isEqualTo("Meditation Room");
        assertThat(response.category()).isEqualTo(FacilityCategory.WELLNESS);
        assertThat(response.active()).isTrue();
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        FacilityCreateRequest request = new FacilityCreateRequest(
                "Meditation Room", FacilityCategory.WELLNESS, null, null, true
        );
        given(facilityRepository.existsByName(request.name())).willReturn(true);

        assertThatThrownBy(() -> facilityService.createFacility(request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value())
                                .isEqualTo(HttpStatus.CONFLICT.value()));
        verify(facilityRepository, never()).save(any(Facility.class));
    }

    @Test
    void partiallyUpdatesFacility() {
        Facility facility = facility(3L, "Old Name", FacilityCategory.ETC, true);
        FacilityUpdateRequest request = new FacilityUpdateRequest(
                "Tea Lounge", FacilityCategory.FOOD, null, null, false
        );
        given(facilityRepository.findById(3L)).willReturn(Optional.of(facility));
        given(facilityRepository.existsByNameAndIdNot("Tea Lounge", 3L)).willReturn(false);

        FacilityDetailResponse response = facilityService.updateFacility(3L, request);

        assertThat(response.name()).isEqualTo("Tea Lounge");
        assertThat(response.category()).isEqualTo(FacilityCategory.FOOD);
        assertThat(response.active()).isFalse();
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingFacility() {
        given(facilityRepository.findById(99L)).willReturn(Optional.empty());
        FacilityUpdateRequest request = new FacilityUpdateRequest(
                null, null, null, null, false
        );

        assertThatThrownBy(() -> facilityService.updateFacility(99L, request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode().value())
                                .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    private Facility facility(
            Long id,
            String name,
            FacilityCategory category,
            boolean active
    ) {
        Facility facility = Facility.create(name, category, null, null, active);
        ReflectionTestUtils.setField(facility, "id", id);
        return facility;
    }
}
