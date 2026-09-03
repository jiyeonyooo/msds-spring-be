-- =====================================================================
-- 회원 목업 데이터 삭제 (mock-users.sql 되돌리기)
--
-- 주의
--   1) inquiries.user_id 는 users 를 참조하는 FK다. 목업 회원이 남긴 문의를
--      먼저 지우지 않으면 FK 제약으로 삭제가 실패한다. 아래에서 같이 지운다.
--   2) resv.member_id 는 FK가 없이 값만 들고 있다. 목업 회원의 예약이 남아
--      있으면 삭제 후 그 예약이 존재하지 않는 회원을 가리키게 된다.
--      아래 확인 쿼리가 0건인지 먼저 보고 실행할 것.
--
--      SELECT COUNT(*) FROM resv
--       WHERE member_id IN (SELECT id FROM users WHERE email IN (
--             'admin@msds.com','manager@msds.com','hong@example.com',
--             'minji@example.com','jiyeon@example.com','seojun@example.com',
--             'haeun@example.com','dohyun@example.com','yerin@example.com',
--             'taemin@example.com','sujin@example.com'));
-- =====================================================================

DELETE FROM inquiries
WHERE user_id IN (
    SELECT id FROM users WHERE email IN (
        'admin@msds.com', 'manager@msds.com', 'hong@example.com',
        'minji@example.com', 'jiyeon@example.com', 'seojun@example.com',
        'haeun@example.com', 'dohyun@example.com', 'yerin@example.com',
        'taemin@example.com', 'sujin@example.com')
);

DELETE FROM users
WHERE email IN (
    'admin@msds.com', 'manager@msds.com', 'hong@example.com',
    'minji@example.com', 'jiyeon@example.com', 'seojun@example.com',
    'haeun@example.com', 'dohyun@example.com', 'yerin@example.com',
    'taemin@example.com', 'sujin@example.com');
