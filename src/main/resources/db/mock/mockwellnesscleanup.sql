-- =====================================================================
-- 마음 기록(웰니스 체크) 목업 데이터 삭제 (mockwellness.sql 되돌리기)
--
-- 삭제 순서: 답변 -> 체크 (자식부터)
-- 기본 설문과 문항, 회원 데이터는 공용이므로 삭제하지 않는다.
--
-- 주의
--   아래 목업 계정으로 SQL 실행 후 새로 작성한 마음 기록도 함께 삭제된다.
--   로컬·개발 DB에서만 실행한다.
-- =====================================================================

DELETE FROM wellness_answers
WHERE wellness_check_id IN (
    SELECT wc.id
    FROM wellness_checks wc
    JOIN users u ON u.id = wc.member_id
    WHERE u.email IN (
        'hong@example.com',
        'minji@example.com',
        'jiyeon@example.com',
        'seojun@example.com',
        'haeun@example.com'
    )
);
DELETE FROM wellness_checks
WHERE member_id IN (
    SELECT id
    FROM users
    WHERE email IN (
        'hong@example.com',
        'minji@example.com',
        'jiyeon@example.com',
        'seojun@example.com',
        'haeun@example.com'
    )
);
