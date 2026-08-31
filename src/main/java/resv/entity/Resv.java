package resv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import resv.enums.ResvStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resv", uniqueConstraints = @UniqueConstraint(name = "uk_resv_number", columnNames = "resv_number"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resv_id")
    private Long resvId;

    @Column(name = "room_units_id", nullable = false)
    private Long roomUnitsId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "resv_number", nullable = false, length = 30, unique = true)
    private String resvNumber;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "price_per_night", nullable = false)
    private Integer pricePerNight;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "resv_status", nullable = false, length = 20)
    private ResvStatus resvStatus;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Resv(Long roomUnitsId, Long memberId, String resvNumber, LocalDate checkInDate,
                LocalDate checkOutDate, Integer guestCount, Integer pricePerNight,
                Integer totalPrice, ResvStatus resvStatus) {
        this.roomUnitsId = roomUnitsId;
        this.memberId = memberId;
        this.resvNumber = resvNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.pricePerNight = pricePerNight;
        this.totalPrice = totalPrice;
        this.resvStatus = resvStatus;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
