package meditation_program.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "picture_url", length = 512)
    private String pictureUrl;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer remain;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProgramStatus status;

    @Version
    private Integer version;

    @CreatedDate
    @Column(name= "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Program(String name, String pictureUrl, Integer capacity) {
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.capacity = capacity;
        this.remain = capacity;
        this.status = ProgramStatus.OPEN;
    }

    public void reserve(int quantity) {
        if (this.remain < quantity) {
            throw new IllegalStateException("잔여 인원이 부족합니다.");
        }
        this.remain -= quantity;
        if (this.remain == 0) this.status = ProgramStatus.CLOSED;
    }

    public void cancelReservation(int quantity) {
        this.remain += quantity;
        this.status = ProgramStatus.OPEN;
    }

    public void updateInfo(String name, String pictureUrl, Integer capacity) {
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.capacity = capacity;
    }
}
