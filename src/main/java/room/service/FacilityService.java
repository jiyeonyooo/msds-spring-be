package room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import room.dto.request.FacilityCreateRequest;
import room.dto.request.FacilityUpdateRequest;
import room.dto.response.FacilityDetailResponse;
import room.dto.response.FacilitySummaryResponse;
import room.entity.Facility;
import room.entity.enums.FacilityCategory;
import room.repository.FacilityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public List<FacilitySummaryResponse> getFacilities(FacilityCategory category) {
        List<Facility> facilities = category == null
                ? facilityRepository.findAllByActiveTrueOrderByCategoryAscNameAsc()
                : facilityRepository.findAllByCategoryAndActiveTrueOrderByNameAsc(category);

        return facilities.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional
    public FacilityDetailResponse createFacility(FacilityCreateRequest request) {
        if (facilityRepository.existsByName(request.name())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Facility name already exists: " + request.name()
            );
        }

        Facility facility = Facility.create(
                request.name(),
                request.category(),
                request.description(),
                request.imageUrl(),
                request.active()
        );

        return toDetailResponse(facilityRepository.save(facility));
    }

    @Transactional
    public FacilityDetailResponse updateFacility(Long facilityId, FacilityUpdateRequest request) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Facility not found: " + facilityId
                ));

        if (request.name() != null
                && facilityRepository.existsByNameAndIdNot(request.name(), facilityId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Facility name already exists: " + request.name()
            );
        }

        facility.update(
                request.name(),
                request.category(),
                request.description(),
                request.imageUrl(),
                request.active()
        );

        return toDetailResponse(facility);
    }

    private FacilitySummaryResponse toSummaryResponse(Facility facility) {
        return new FacilitySummaryResponse(
                facility.getId(),
                facility.getName(),
                facility.getCategory(),
                facility.getDescription(),
                facility.getImageUrl()
        );
    }

    private FacilityDetailResponse toDetailResponse(Facility facility) {
        return new FacilityDetailResponse(
                facility.getId(),
                facility.getName(),
                facility.getCategory(),
                facility.getDescription(),
                facility.getImageUrl(),
                facility.getActive(),
                facility.getCreatedAt(),
                facility.getUpdatedAt()
        );
    }
}
