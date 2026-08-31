package room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private FacilitySummaryResponse toSummaryResponse(Facility facility) {
        return new FacilitySummaryResponse(
                facility.getId(),
                facility.getName(),
                facility.getCategory(),
                facility.getDescription(),
                facility.getImageUrl()
        );
    }
}
