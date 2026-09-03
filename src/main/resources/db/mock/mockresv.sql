-- =====================================================================
-- 예약 목업 데이터 (MSDS)
--
-- 대상 테이블: resv
--
-- 선행 조건 (반드시 먼저 실행할 것)
--   1) mock-users.sql          -- member_id 를 email 로 찾는다
--   2) mock-room-facility.sql  -- room_units_id 를 (객실명, 호실번호) 로 찾는다
--   선행 데이터가 없으면 JOIN 이 걸리지 않아 0건만 들어간다.
--
-- 사용법
--   mysql -u <user> -p <database> < mock-resv.sql
--
-- 특징
--   - 스프링 부트가 자동 실행하지 않는 위치(db/mock)에 둔다.
--   - PK를 직접 지정하지 않고 resv_number 기준으로 삽입하므로
--     AUTO_INCREMENT 값을 건드리지 않고 기존 예약과 충돌하지 않는다.
--   - 예약번호가 이미 있으면 건너뛴다(WHERE NOT EXISTS). 반복 실행해도 안전하다.
--   - MySQL 8 / H2(MODE=MySQL) 양쪽에서 동작한다.
--
-- 데이터 규칙 (애플리케이션 로직과 맞춰둔 값)
--   - resv_number 형식: RSV-yyyyMMdd-<16자리 대문자 HEX>
--   - price_per_night = 해당 객실의 base_price
--   - total_price     = price_per_night * 숙박일수(check_out - check_in)
--   - guest_count    <= 해당 객실의 max_guests
--   - RESERVED 예약은 ACTIVE 호실에만 배정하고, 같은 호실에서 서로 겹치지 않는다.
--     (겹치면 예약 가능 조회 결과가 실제와 어긋난다)
--   - CANCELLED 예약은 가용성 계산에서 빠지므로 겹쳐도 무방하다. cancelled_at 을 채운다.
--
-- 관리자 예약 검색(searchForAdmin) 날짜 필터 확인용 배치
--   검색 기간을 2026-10-10 ~ 2026-10-15 로 두면 아래처럼 갈린다.
--     조회됨 : 10-11~10-13(완전 포함), 10-08~10-11(시작일 겹침),
--              10-14~10-18(종료일 겹침), 10-05~10-20(기간 전체 감쌈)
--     제외됨 : 10-06~10-10(체크아웃일이 검색 시작일과 맞닿기만 함),
--              10-15~10-18(체크인일이 검색 종료일과 맞닿기만 함),
--              09-20~09-22, 11-02~11-05(전혀 겹치지 않음)
--
-- 되돌리기: mock-resv-cleanup.sql
-- =====================================================================

INSERT INTO resv (room_units_id, member_id, resv_number, check_in_date, check_out_date,
                  guest_count, price_per_night, total_price, resv_status, cancelled_at,
                  created_at, updated_at)
SELECT u.id, m.id, t.resv_number, t.check_in_date, t.check_out_date,
       t.guest_count, t.price_per_night, t.total_price, t.resv_status, t.cancelled_at,
       t.created_at, t.created_at
FROM (
    -- ===== 검색 기간(10-10 ~ 10-15)과 겹쳐서 "조회되어야 하는" 예약 =====

    -- 기간에 완전히 포함
    SELECT '오션 사일런스 스위트' AS room_name, '201' AS room_number,
           'hong@example.com'     AS member_email,
           'RSV-20261011-4C1F59A821D04E7B' AS resv_number,
           '2026-10-11' AS check_in_date, '2026-10-13' AS check_out_date,
           2 AS guest_count, 180000 AS price_per_night, 360000 AS total_price,
           'RESERVED' AS resv_status,
           CAST(NULL AS DATETIME) AS cancelled_at,
           '2026-09-15 10:24:00' AS created_at
    UNION ALL
    -- 시작일 쪽에서만 겹침 (검색 기간 전에 체크인해서 기간 안에서 체크아웃)
    SELECT '오션 사일런스 스위트', '202', 'minji@example.com',
           'RSV-20261008-9B7D2E4416CA05F3',
           '2026-10-08', '2026-10-11', 2, 180000, 540000, 'RESERVED', NULL,
           '2026-09-12 16:03:00'
    UNION ALL
    -- 종료일 쪽에서만 겹침 (검색 기간 안에서 체크인해서 기간이 끝난 뒤 체크아웃)
    SELECT '포레스트 트윈', '301', 'jiyeon@example.com',
           'RSV-20261014-E05A73C9B812DD64',
           '2026-10-14', '2026-10-18', 2, 140000, 560000, 'RESERVED', NULL,
           '2026-09-20 09:41:00'
    UNION ALL
    -- 검색 기간을 통째로 감쌈 (장기 투숙)
    SELECT '포레스트 트윈', '302', 'seojun@example.com',
           'RSV-20261005-7F3B18D0A6E95C22',
           '2026-10-05', '2026-10-20', 2, 140000, 2100000, 'RESERVED', NULL,
           '2026-08-30 20:17:00'

    -- ===== 경계에서만 맞닿아 "제외되어야 하는" 예약 =====

    UNION ALL
    -- 체크아웃일 == 검색 시작일. 체크아웃 당일은 숙박에 포함되지 않으므로 겹치지 않는다.
    SELECT '스틸 싱글룸', '101', 'haeun@example.com',
           'RSV-20261006-2D6C90FB35E17A48',
           '2026-10-06', '2026-10-10', 1, 110000, 440000, 'RESERVED', NULL,
           '2026-09-18 11:55:00'
    UNION ALL
    -- 체크인일 == 검색 종료일.
    SELECT '스틸 싱글룸', '102', 'dohyun@example.com',
           'RSV-20261015-B814AF7260C3E9D1',
           '2026-10-15', '2026-10-18', 1, 110000, 330000, 'RESERVED', NULL,
           '2026-09-22 08:09:00'

    -- ===== 검색 기간과 전혀 겹치지 않는 예약 =====

    UNION ALL
    SELECT '리트릿 하우스', '401', 'yerin@example.com',
           'RSV-20260920-A73E5D1CF0928B46',
           '2026-09-20', '2026-09-22', 4, 320000, 640000, 'RESERVED', NULL,
           '2026-09-01 13:30:00'
    UNION ALL
    SELECT '가든 더블룸', '103', 'taemin@example.com',
           'RSV-20261102-C29F604B7AE31D85',
           '2026-11-02', '2026-11-05', 2, 150000, 450000, 'RESERVED', NULL,
           '2026-09-25 19:48:00'

    -- ===== 과거 숙박 이력 (지난 예약 목록·회원 활동 내역 확인용) =====

    UNION ALL
    SELECT '오션 사일런스 스위트', '201', 'hong@example.com',
           'RSV-20260710-16D8B34E9C07F2A5',
           '2026-07-10', '2026-07-12', 3, 180000, 360000, 'RESERVED', NULL,
           '2026-06-28 21:12:00'
    UNION ALL
    SELECT '포레스트 트윈', '301', 'sujin@example.com',
           'RSV-20260805-5E0A9721DB4C86F3',
           '2026-08-05', '2026-08-08', 2, 140000, 420000, 'RESERVED', NULL,
           '2026-07-19 07:36:00'
    UNION ALL
    -- 임박한 예약
    SELECT '가든 더블룸', '104', 'hong@example.com',
           'RSV-20260904-38C7E1A5B920D64F',
           '2026-09-04', '2026-09-06', 2, 150000, 300000, 'RESERVED', NULL,
           '2026-08-27 15:02:00'
    UNION ALL
    -- 연말 장기 예약
    SELECT '리트릿 하우스', '401', 'minji@example.com',
           'RSV-20261224-D41B6F8305A2C79E',
           '2026-12-24', '2026-12-28', 6, 320000, 1280000, 'RESERVED', NULL,
           '2026-09-02 22:41:00'

    -- ===== 취소된 예약 (상태 필터·복구 기능 확인용) =====
    -- 가용성 계산에서 빠지므로 RESERVED 예약과 기간이 겹쳐도 된다.

    UNION ALL
    SELECT '오션 사일런스 스위트', '201', 'jiyeon@example.com',
           'RSV-20261011-6A2F0C95E7B138D0',
           '2026-10-11', '2026-10-14', 2, 180000, 540000, 'CANCELLED',
           '2026-09-19 14:26:00', '2026-09-10 12:18:00'
    UNION ALL
    SELECT '리트릿 하우스', '401', 'seojun@example.com',
           'RSV-20260925-0F58D9A3C641E27B',
           '2026-09-25', '2026-09-27', 5, 320000, 640000, 'CANCELLED',
           '2026-09-01 09:03:00', '2026-08-14 18:55:00'
) AS t
JOIN rooms r ON r.name = t.room_name
JOIN room_units u ON u.room_id = r.id AND u.room_number = t.room_number
JOIN users m ON m.email = t.member_email
WHERE NOT EXISTS (SELECT 1 FROM resv v WHERE v.resv_number = t.resv_number);
