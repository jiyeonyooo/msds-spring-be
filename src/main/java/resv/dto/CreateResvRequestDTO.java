package resv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

//예약 내역 생성 Request DTO
@Data
@AllArgsConstructor
public class CreateResvRequestDTO {
    long roomId;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    int guestCount;
    int quantity;
}
