package member.inquiry.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import member.user.domain.User;

import java.time.LocalDateTime;

/**
 * 문의(Inquiry) 도메인 엔티티.
 * 회원이 작성하고, 관리자가 답변을 등록한다.
 * 답변은 별도 테이블로 분리하지 않고 컬럼으로 함께 관리한다(1문의-1답변 구조).
 */
@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자. 지연 로딩으로 설정해 목록 조회 시 불필요한 조인을 피한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    @Lob
    private String answerContent;

    private LocalDateTime answeredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Inquiry(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.status = InquiryStatus.WAITING;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // 관리자 답변 등록 (Dirty Checking으로 반영됨)
    public void answer(String answerContent) {
        this.answerContent = answerContent;
        this.answeredAt = LocalDateTime.now();
        this.status = InquiryStatus.ANSWERED;
    }

    @PreUpdate
    private void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 본인 문의인지 확인 (조회 권한 체크용)
    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }
}