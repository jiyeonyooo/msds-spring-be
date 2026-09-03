package resv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import resv.entity.Resv;
import resv.enums.ResvStatus;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ResvRepository extends JpaRepository<Resv, Long> {
    Optional<Resv> findByResvNumber(String resvNumber);

    boolean existsByResvNumber(String resvNumber);

    boolean existsByRoomUnitsIdAndResvStatus(Long roomUnitsId, ResvStatus resvStatus);

    Page<Resv> findByMemberIdAndResvStatus(Long memberId, ResvStatus resvStatus, Pageable pageable);

    Page<Resv> findByMemberId(Long memberId, Pageable pageable);

    // 날짜 필터는 검색 기간에 완전히 포함된 예약이 아니라, 검색 기간과 하루라도 겹치는 예약을 모두 찾는다.
    // 예약 기간은 체크인 포함 / 체크아웃 미포함(반열림 구간)이므로 경계에서만 맞닿는 예약은 겹치지 않는 것으로 본다.
    @Query("""
            select resv from Resv resv
            join User member on member.id = resv.memberId
            where (:resvStatus is null or resv.resvStatus = :resvStatus)
              and (:searchFromDate is null or resv.checkOutDate > :searchFromDate)
              and (:searchToDate is null or resv.checkInDate < :searchToDate)
              and (:keyword is null or lower(resv.resvNumber) like lower(concat('%', :keyword, '%'))
                   or lower(member.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<Resv> searchForAdmin(
            @Param("resvStatus") ResvStatus resvStatus,
            @Param("searchFromDate") LocalDate searchFromDate,
            @Param("searchToDate") LocalDate searchToDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 관리자 회원 목록에서 쓰는 회원별 예약 건수. 행마다 조회하지 않도록 한 번에 묶어서 센다.
    // 반환 각 행은 [회원 ID, 예약 건수] 형태.
    @Query("select resv.memberId, count(resv) from Resv resv where resv.memberId in :memberIds group by resv.memberId")
    List<Object[]> countByMemberIds(@Param("memberIds") List<Long> memberIds);

    // 회원 상세에서 쓰는 단일 회원 예약 건수.
    long countByMemberId(Long memberId);

    /** 관리자 회원 활동 내역용 예약 요약. 객실명까지 한 번의 조회로 가져온다. */
    interface MemberResvSummary {
        Long getResvId();
        String getResvNumber();
        String getRoomName();
        String getRoomNumber();
        LocalDate getCheckInDate();
        LocalDate getCheckOutDate();
        Integer getGuestCount();
        Integer getTotalPrice();
        ResvStatus getResvStatus();
        LocalDateTime getCreatedAt();
    }

    @Query("""
            select resv.resvId as resvId, resv.resvNumber as resvNumber, room.name as roomName,
                   roomUnit.roomNumber as roomNumber, resv.checkInDate as checkInDate,
                   resv.checkOutDate as checkOutDate, resv.guestCount as guestCount,
                   resv.totalPrice as totalPrice, resv.resvStatus as resvStatus, resv.createdAt as createdAt
            from Resv resv
            join RoomUnit roomUnit on roomUnit.id = resv.roomUnitsId
            join roomUnit.room room
            where resv.memberId = :memberId
            order by resv.checkInDate desc
            """)
    List<MemberResvSummary> findSummariesByMemberId(@Param("memberId") Long memberId);
}