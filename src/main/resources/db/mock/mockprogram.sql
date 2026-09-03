-- =====================================================================
-- 명상 프로그램 목업 데이터 (MSDS)
--
-- 대상 테이블: program, program_reservation, reviews
--
-- 선행 조건
--   1) 애플리케이션을 한 번 실행해 JPA 테이블을 생성한다.
--   2) mockusers.sql을 먼저 실행한다. 회원이 없으면 신청/후기는 삽입되지 않는다.
--
-- 사용법
--   mysql -u <user> -p <database> < mockprogram.sql
--   (또는 DBeaver/Workbench에서 스크립트 전체 실행)
--
-- 특징
--   - 프로그램명과 (프로그램, 회원, 생성시각)를 기준으로 중복 삽입을 막는다.
--   - 마지막 UPDATE에서 실제 RESERVED 신청 수에 맞게 remain/status를 보정한다.
--   - 프론트엔드의 프로그램별 이미지/설명 매핑과 같은 영문명을 사용한다.
--   - MySQL 8 / H2(MODE=MySQL) 양쪽에서 동작한다.
--
-- 기대 데이터
--   program 4 / program_reservation 10 / reviews 4
--   Ocean Breathing은 정원 마감(CLOSED), 나머지는 신청 가능(OPEN)
--
-- 되돌리기: mockprogramcleanup.sql
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. program
--    status: OPEN / CLOSED / DELETED
--    version: JPA 낙관적 잠금(@Version)의 초기값 0
-- ---------------------------------------------------------------------
INSERT INTO program (name, capacity, remain, status, version,
                     created_at, updated_at)
SELECT t.name, t.capacity, t.capacity, 'OPEN', 0,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT 'Morning Silence Meditation' AS name, 12 AS capacity
    UNION ALL
    SELECT 'Ocean Breathing', 10
    UNION ALL
    SELECT 'Slow Walking Practice', 8
    UNION ALL
    SELECT 'Evening Tea Meditation', 10
) AS t
WHERE NOT EXISTS (SELECT 1 FROM program p WHERE p.name = t.name);


-- ---------------------------------------------------------------------
-- 2. program_reservation
--    RESERVED와 CANCELLED를 함께 넣어 목록/취소/관리자 필터를 확인한다.
-- ---------------------------------------------------------------------
INSERT INTO program_reservation (program_id, user_id, quantity, status, cancelled_at,
                                 created_at, updated_at)
SELECT p.id, u.id, t.quantity, t.status, t.cancelled_at,
       t.created_at, t.updated_at
FROM (
    SELECT 'Morning Silence Meditation' AS program_name,
           'hong@example.com' AS user_email,
           2 AS quantity, 'RESERVED' AS status,
           CAST(NULL AS DATETIME) AS cancelled_at,
           '2026-08-20 09:15:00' AS created_at,
           '2026-08-20 09:15:00' AS updated_at
    UNION ALL
    SELECT 'Morning Silence Meditation', 'minji@example.com',
           1, 'RESERVED', NULL, '2026-08-21 13:40:00', '2026-08-21 13:40:00'
    UNION ALL
    SELECT 'Morning Silence Meditation', 'jiyeon@example.com',
           1, 'CANCELLED', '2026-08-24 18:30:00',
           '2026-08-22 10:05:00', '2026-08-24 18:30:00'

    UNION ALL
    SELECT 'Ocean Breathing', 'seojun@example.com',
           2, 'RESERVED', NULL, '2026-08-23 08:20:00', '2026-08-23 08:20:00'
    UNION ALL
    SELECT 'Ocean Breathing', 'haeun@example.com',
           3, 'RESERVED', NULL, '2026-08-23 11:10:00', '2026-08-23 11:10:00'
    UNION ALL
    SELECT 'Ocean Breathing', 'dohyun@example.com',
           2, 'RESERVED', NULL, '2026-08-24 16:45:00', '2026-08-24 16:45:00'
    UNION ALL
    SELECT 'Ocean Breathing', 'yerin@example.com',
           3, 'RESERVED', NULL, '2026-08-25 19:05:00', '2026-08-25 19:05:00'

    UNION ALL
    SELECT 'Slow Walking Practice', 'taemin@example.com',
           1, 'RESERVED', NULL, '2026-08-26 07:35:00', '2026-08-26 07:35:00'
    UNION ALL
    SELECT 'Slow Walking Practice', 'sujin@example.com',
           2, 'RESERVED', NULL, '2026-08-27 12:25:00', '2026-08-27 12:25:00'

    UNION ALL
    SELECT 'Evening Tea Meditation', 'hong@example.com',
           2, 'CANCELLED', '2026-08-30 20:15:00',
           '2026-08-28 14:50:00', '2026-08-30 20:15:00'
) AS t
JOIN program p ON p.name = t.program_name
JOIN users u ON u.email = t.user_email
WHERE NOT EXISTS (
    SELECT 1
    FROM program_reservation pr
    WHERE pr.program_id = p.id
      AND pr.user_id = u.id
      AND pr.created_at = t.created_at
);


-- ---------------------------------------------------------------------
-- 3. reviews
--    program_reservation_id에 유니크 제약이 있어 신청 1건당 후기 1개만 가능하다.
-- ---------------------------------------------------------------------
INSERT INTO reviews (program_reservation_id, content, created_at, updated_at)
SELECT pr.id, t.content, t.created_at, t.created_at
FROM (
    SELECT 'Morning Silence Meditation' AS program_name,
           'hong@example.com' AS user_email,
           '아침 햇살 속에서 호흡에 집중하니 하루를 차분하게 시작할 수 있었어요.' AS content,
           '2026-08-29 09:10:00' AS created_at
    UNION ALL
    SELECT 'Morning Silence Meditation', 'minji@example.com',
           '초보자도 따라가기 쉬운 안내 덕분에 긴장을 편안하게 내려놓았습니다.',
           '2026-08-30 11:25:00'
    UNION ALL
    SELECT 'Ocean Breathing', 'seojun@example.com',
           '파도 소리와 호흡이 자연스럽게 이어져 오래 기억에 남는 시간이었습니다.',
           '2026-08-31 18:40:00'
    UNION ALL
    SELECT 'Slow Walking Practice', 'taemin@example.com',
           '천천히 걷는 것만으로도 몸의 감각이 또렷해지는 경험을 했습니다.',
           '2026-09-01 13:05:00'
) AS t
JOIN program p ON p.name = t.program_name
JOIN users u ON u.email = t.user_email
JOIN program_reservation pr
  ON pr.program_id = p.id
 AND pr.user_id = u.id
 AND pr.status = 'RESERVED'
WHERE NOT EXISTS (
    SELECT 1 FROM reviews r WHERE r.program_reservation_id = pr.id
);


-- ---------------------------------------------------------------------
-- 4. 프로그램 잔여 인원과 상태 보정
--    CANCELLED 신청은 정원 계산에서 제외한다.
-- ---------------------------------------------------------------------
UPDATE program p
SET remain = GREATEST(
        0,
        p.capacity - COALESCE((
            SELECT SUM(pr.quantity)
            FROM program_reservation pr
            WHERE pr.program_id = p.id
              AND pr.status = 'RESERVED'
        ), 0)
    ),
    status = CASE
        WHEN COALESCE((
            SELECT SUM(pr.quantity)
            FROM program_reservation pr
            WHERE pr.program_id = p.id
              AND pr.status = 'RESERVED'
        ), 0) >= p.capacity THEN 'CLOSED'
        ELSE 'OPEN'
    END,
    updated_at = CURRENT_TIMESTAMP
WHERE p.name IN (
    'Morning Silence Meditation',
    'Ocean Breathing',
    'Slow Walking Practice',
    'Evening Tea Meditation'
);
