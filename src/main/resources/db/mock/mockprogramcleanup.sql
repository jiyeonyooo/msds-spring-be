-- =====================================================================
-- 명상 프로그램 목업 데이터 삭제 (mockprogram.sql 되돌리기)
--
-- 삭제 순서: 후기 -> 프로그램 신청 -> 프로그램 (자식부터)
--
-- 주의
--   아래 프로그램에 목업 실행 후 실제 신청이 추가된 경우 그 신청과 후기도
--   함께 삭제된다. 로컬·개발 DB에서만 실행한다.
-- =====================================================================

DELETE FROM reviews
WHERE program_reservation_id IN (
    SELECT pr.id
    FROM program_reservation pr
    JOIN program p ON p.id = pr.program_id
    WHERE p.name IN (
        'Morning Silence Meditation',
        'Ocean Breathing',
        'Slow Walking Practice',
        'Evening Tea Meditation'
    )
);

DELETE FROM program_reservation
WHERE program_id IN (
    SELECT id
    FROM program
    WHERE name IN (
        'Morning Silence Meditation',
        'Ocean Breathing',
        'Slow Walking Practice',
        'Evening Tea Meditation'
    )
);

DELETE FROM program
WHERE name IN (
    'Morning Silence Meditation',
    'Ocean Breathing',
    'Slow Walking Practice',
    'Evening Tea Meditation'
);
