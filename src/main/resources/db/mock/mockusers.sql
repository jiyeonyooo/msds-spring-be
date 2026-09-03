-- =====================================================================
-- 회원 목업 데이터 (MSDS)
--
-- 대상 테이블: users
--
-- 로그인 비밀번호는 전 계정 공통으로  Test1234!  이다.
-- password 컬럼에는 BCryptPasswordEncoder(강도 10)로 만든 해시를 넣는다.
-- SecurityConfig가 BCryptPasswordEncoder를 쓰므로 그대로 로그인된다.
--
--   POST /api/auth/login
--   { "email": "hong@example.com", "password": "Test1234!" }
--
-- 사용법
--   mysql -u <user> -p <database> < mock-users.sql
--
-- 특징
--   - 스프링 부트가 자동 실행하지 않는 위치(db/mock)에 둔다.
--   - PK를 직접 지정하지 않고 email 기준으로 삽입하므로
--     AUTO_INCREMENT 값을 건드리지 않고 기존 회원과 충돌하지 않는다.
--   - 이메일이 이미 있으면 건너뛴다(WHERE NOT EXISTS). 여러 번 실행해도 안전하다.
--   - MySQL 8 / H2(MODE=MySQL) 양쪽에서 동작한다.
--
-- 주의
--   - 목업 계정은 비밀번호가 공개되어 있다. 로컬·개발 DB에서만 쓸 것.
--   - 이메일은 소문자로만 넣는다. 로그인 시 입력 이메일을 소문자로 정규화해
--     조회하므로 대문자가 섞이면 로그인되지 않는다.
--
-- 되돌리기: mock-users-cleanup.sql
-- =====================================================================

INSERT INTO users (email, password, name, phone_number, role, created_at, updated_at)
SELECT t.email, t.password, t.name, t.phone_number, t.role, t.created_at, t.updated_at
FROM (
    -- 관리자 2명 --------------------------------------------------------
    SELECT 'admin@msds.com'   AS email,
           '$2a$10$Upkt/RIMF1mO0FdAurmdy.3r4gsGCVDWFfBWAEdLQJnYmV9TPEpYu' AS password,
           '관리자'            AS name,
           '010-0000-0001'    AS phone_number,
           'ADMIN'            AS role,
           '2026-01-02 09:00:00' AS created_at,
           '2026-01-02 09:00:00' AS updated_at
    UNION ALL
    SELECT 'manager@msds.com', '$2a$10$bePCCCg.EyEs7kSde2HuKeo/n3J/Zjz1OlA//4.XQEVQYvKgfi56u',
           '운영매니저', '010-0000-0002', 'ADMIN', '2026-01-05 10:30:00', '2026-01-05 10:30:00'

    -- 일반 회원 9명 -----------------------------------------------------
    UNION ALL
    SELECT 'hong@example.com', '$2a$10$8BswiJJQxkrKPWB4M/376eoltuNNjGbVj6FYuIuwNKLvbNsFceUnW',
           '홍길동', '010-1111-2222', 'USER', '2026-02-11 14:12:00', '2026-02-11 14:12:00'
    UNION ALL
    SELECT 'minji@example.com', '$2a$10$sAO4yRm402kuj925CPZvPOw8so/T1fSL.86jhNiR1VzjUqPngvq2a',
           '김민지', '010-3333-4444', 'USER', '2026-02-27 09:41:00', '2026-02-27 09:41:00'
    UNION ALL
    SELECT 'jiyeon@example.com', '$2a$10$qrXDA23FcTqyGx7OXQcIDeksEVHDmmA9u9LYn7yFRDJThSVJJ8AsG',
           '유지연', '010-5555-6666', 'USER', '2026-03-14 18:05:00', '2026-03-14 18:05:00'
    UNION ALL
    SELECT 'seojun@example.com', '$2a$10$FUahWVSS.CH8EobMbknFU.3zcMkX56Gv7VGmfrjNwzxJNOtqPjf.y',
           '박서준', '010-7777-8888', 'USER', '2026-04-02 11:20:00', '2026-04-02 11:20:00'
    UNION ALL
    SELECT 'haeun@example.com', '$2a$10$2AO7QDq86KQUOMvNbVvzvubbdN2iy2zbc7tAogVs2uJ7kNOF4hsDW',
           '이하은', '010-2222-3333', 'USER', '2026-05-19 16:48:00', '2026-05-19 16:48:00'
    UNION ALL
    SELECT 'dohyun@example.com', '$2a$10$B0bigLcMhH5O5uCQAZyt1.7CcjV/zcTaNunbqEuDhMO8L9beveRCS',
           '최도현', '010-4444-5555', 'USER', '2026-06-08 08:15:00', '2026-06-08 08:15:00'
    UNION ALL
    SELECT 'yerin@example.com', '$2a$10$Sf5jHtcI/AYRKRkwZUul3.VPB13AuQeCN.RaYYNtCIQhw1nGrVzvm',
           '정예린', '010-6666-7777', 'USER', '2026-07-01 20:33:00', '2026-07-01 20:33:00'
    UNION ALL
    SELECT 'taemin@example.com', '$2a$10$5ZCPPv4rwN6nf.CSGRTjF.DPuFJj8rNf7LzBmZbc9fzwhnnubQaWS',
           '강태민', '010-8888-9999', 'USER', '2026-08-12 13:07:00', '2026-08-12 13:07:00'
    UNION ALL
    SELECT 'sujin@example.com', '$2a$10$dUbarxzY1OYUqm57c6TGA.8XzeAli2vhDyTnckWWry375bzG8ccym',
           '문수진', '010-9999-0000', 'USER', '2026-09-01 07:52:00', '2026-09-01 07:52:00'
) AS t
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = t.email);
