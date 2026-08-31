package member.inquiry.repository;

import member.inquiry.domain.Inquiry;
import member.inquiry.domain.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Inquiry 엔티티 데이터 접근 계층.
 *
 * Inquiry.user는 지연 로딩(LAZY)인데 InquiryResponse가 작성자 이메일을 사용하므로,
 * 목록/상세 조회는 메소드 이름 규칙 대신 JPQL의 join fetch로 작성자까지 한 번에 조회한다.
 * (그냥 findAll()로 조회하면 문의 N건마다 회원 조회 쿼리가 추가로 나가는 N+1 문제가 발생)
 */
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 특정 회원이 작성한 문의 목록 (최신순). 마이페이지 "내 문의 내역" 조회용.
    @Query("select i from Inquiry i join fetch i.user u "
            + "where u.id = :userId order by i.createdAt desc")
    List<Inquiry> findAllByUserIdWithUser(@Param("userId") Long userId);

    // 문의 상세 조회 (작성자 정보 포함). 상세 조회/답변 등록에서 사용.
    @Query("select i from Inquiry i join fetch i.user where i.id = :inquiryId")
    Optional<Inquiry> findByIdWithUser(@Param("inquiryId") Long inquiryId);

    // 관리자용 전체 문의 목록 (최신순).
    @Query("select i from Inquiry i join fetch i.user order by i.createdAt desc")
    List<Inquiry> findAllWithUser();

    // 관리자용 상태별 문의 목록 (최신순). 예: 미답변(WAITING) 문의만 조회.
    @Query("select i from Inquiry i join fetch i.user "
            + "where i.status = :status order by i.createdAt desc")
    List<Inquiry> findAllByStatusWithUser(@Param("status") InquiryStatus status);
}
