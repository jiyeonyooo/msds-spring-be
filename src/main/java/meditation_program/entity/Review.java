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
@Table(name = "reviews")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "program_reservation_id", nullable = false, unique = true)
    private ProgramReservation programReservation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Review(ProgramReservation programReservation, String content) {
        this.programReservation = programReservation;
        this.content = content;
    }
}
