package member.inquiry.repository;

import member.inquiry.domain.Inquiry;
import member.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 특정 회원이 작성한 문의 목록 (최신순). 마이페이지 "내 문의 내역" 조회용.
    List<Inquiry> findByUserOrderByCreatedAtDesc(User user);

    // 관리자용 전체 문의 목록 (최신순). findAll()도 있지만 정렬을 명시적으로 고정.
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}