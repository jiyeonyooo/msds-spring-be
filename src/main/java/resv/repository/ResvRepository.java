package resv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import resv.entity.Resv;
import resv.enums.ResvStatus;

import java.util.Optional;
import java.time.LocalDate;

public interface ResvRepository extends JpaRepository<Resv, Long> {
    Optional<Resv> findByResvNumber(String resvNumber);

    boolean existsByResvNumber(String resvNumber);

    boolean existsByRoomUnitsIdAndResvStatus(Long roomUnitsId, ResvStatus resvStatus);

    Page<Resv> findByMemberIdAndResvStatus(Long memberId, ResvStatus resvStatus, Pageable pageable);

    Page<Resv> findByMemberId(Long memberId, Pageable pageable);

    @Query("""
            select resv from Resv resv
            join User member on member.id = resv.memberId
            where (:resvStatus is null or resv.resvStatus = :resvStatus)
              and (:searchFromDate is null or resv.checkInDate >= :searchFromDate)
              and (:searchToDate is null or resv.checkOutDate <= :searchToDate)
              and (:keyword is null or lower(resv.resvNumber) like lower(concat('%', :keyword, '%'))
                   or lower(member.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<Resv> searchForAdmin(
            @Param("resvStatus") ResvStatus resvStatus,
            @Param("searchFromDate") LocalDate searchFromDate,
            @Param("searchToDate") LocalDate searchToDate,
            @Param("keyword") String keyword,
            Pageable pageable);
}
