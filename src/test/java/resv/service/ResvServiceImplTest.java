package resv.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import resv.dto.ResvCancelResponseDTO;
import resv.entity.Resv;
import resv.enums.ResvStatus;
import resv.exception.ResvException;
import resv.repository.ResvRepository;
import resv.repository.ResvRoomUnitRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResvServiceImplTest {

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    @Test
    void adminCancellationUsesInjectedKoreaClock() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-08T15:30:00Z"), KOREA);
        Resv reservation = reservation(LocalDate.of(2026, 9, 10));
        ResvRepository repository = mock(ResvRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));

        ResvServiceImpl service = service(repository, clock);

        ResvCancelResponseDTO response = service.cancelAdminReservation(1L);

        assertThat(response.resvStatus()).isEqualTo("CANCELLED");
        assertThat(response.cancelledAt()).isEqualTo(java.time.LocalDateTime.of(2026, 9, 9, 0, 30));
        assertThat(reservation.getResvStatus()).isEqualTo(ResvStatus.CANCELLED);
    }

    @Test
    void cancellationIsRejectedOnCheckInDate() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-09T15:00:00Z"), KOREA);
        Resv reservation = reservation(LocalDate.of(2026, 9, 10));
        ResvRepository repository = mock(ResvRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));

        ResvServiceImpl service = service(repository, clock);

        assertThatThrownBy(() -> service.cancelAdminReservation(1L))
                .isInstanceOf(ResvException.class)
                .extracting(exception -> ((ResvException) exception).getErrorCode().name())
                .isEqualTo("RESV_CANNOT_CANCEL");
        assertThat(reservation.getResvStatus()).isEqualTo(ResvStatus.RESERVED);
    }

    @Test
    void adminRestoreRestoresCancelledReservationWhenRoomUnitIsAvailable() {
        Resv reservation = reservation(LocalDate.of(2026, 9, 10));
        reservation.cancel(java.time.LocalDateTime.of(2026, 9, 1, 12, 0));
        ResvRepository repository = mock(ResvRepository.class);
        ResvRoomUnitRepository roomUnitRepository = mock(ResvRoomUnitRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomUnitRepository.findAvailableByIdForUpdate(
                101L, reservation.getCheckInDate(), reservation.getCheckOutDate(),
                room.entity.enums.RoomUnitStatus.ACTIVE, ResvStatus.RESERVED))
                .thenReturn(Optional.of(mock(room.entity.RoomUnit.class)));

        ResvServiceImpl service = new ResvServiceImpl(mock(EntityManager.class), repository, roomUnitRepository, Clock.system(KOREA));

        ResvCancelResponseDTO response = service.restoreAdminReservation(1L);

        assertThat(response.resvStatus()).isEqualTo("RESERVED");
        assertThat(response.cancelledAt()).isNull();
        assertThat(reservation.getResvStatus()).isEqualTo(ResvStatus.RESERVED);
    }

    @Test
    void adminRestoreRejectsUnavailableRoomUnit() {
        Resv reservation = reservation(LocalDate.of(2026, 9, 10));
        reservation.cancel(java.time.LocalDateTime.of(2026, 9, 1, 12, 0));
        ResvRepository repository = mock(ResvRepository.class);
        ResvRoomUnitRepository roomUnitRepository = mock(ResvRoomUnitRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomUnitRepository.findAvailableByIdForUpdate(
                101L, reservation.getCheckInDate(), reservation.getCheckOutDate(),
                room.entity.enums.RoomUnitStatus.ACTIVE, ResvStatus.RESERVED))
                .thenReturn(Optional.empty());

        ResvServiceImpl service = new ResvServiceImpl(mock(EntityManager.class), repository, roomUnitRepository, Clock.system(KOREA));

        assertThatThrownBy(() -> service.restoreAdminReservation(1L))
                .isInstanceOf(ResvException.class)
                .extracting(exception -> ((ResvException) exception).getErrorCode().name())
                .isEqualTo("ROOM_NOT_AVAILABLE");
    }

    private ResvServiceImpl service(ResvRepository repository, Clock clock) {
        return new ResvServiceImpl(mock(EntityManager.class), repository, mock(ResvRoomUnitRepository.class), clock);
    }

    private Resv reservation(LocalDate checkInDate) {
        Resv reservation = Resv.builder()
                .roomUnitsId(101L)
                .memberId(10L)
                .resvNumber("RSV-20260910-A13F59C821D04E7B")
                .checkInDate(checkInDate)
                .checkOutDate(checkInDate.plusDays(2))
                .guestCount(2)
                .pricePerNight(80_000)
                .totalPrice(160_000)
                .resvStatus(ResvStatus.RESERVED)
                .build();
        ReflectionTestUtils.setField(reservation, "resvId", 1L);
        return reservation;
    }
}
