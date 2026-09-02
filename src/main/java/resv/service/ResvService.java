package resv.service;

import resv.dto.CreateResvRequestDTO;
import resv.dto.CreateResvResponseDTO;
import resv.dto.AdminResvSearchRequestDTO;
import resv.dto.MyResvSearchRequestDTO;
import resv.dto.MyResvListResponseDTO;
import resv.dto.ResvAvailabilityRequestDTO;
import resv.dto.ResvAvailabilityResponseDTO;
import resv.dto.ResvCancelResponseDTO;
import resv.dto.ResvDetailResponseDTO;
import resv.dto.MyResvDetailResponseDTO;
import resv.dto.ResvListResponseDTO;

public interface ResvService {

    CreateResvResponseDTO create(String memberEmail, CreateResvRequestDTO request);

    ResvAvailabilityResponseDTO getAvailability(ResvAvailabilityRequestDTO request);

    MyResvListResponseDTO getMyReservations(String memberEmail, MyResvSearchRequestDTO request);

    MyResvDetailResponseDTO getMyReservation(String memberEmail, long resvId);

    ResvCancelResponseDTO cancelMyReservation(String memberEmail, long resvId);

    ResvListResponseDTO getAdminReservations(AdminResvSearchRequestDTO request);

    ResvDetailResponseDTO getAdminReservation(long resvId);

    ResvCancelResponseDTO cancelAdminReservation(long resvId);

    ResvCancelResponseDTO restoreAdminReservation(long resvId);
}
