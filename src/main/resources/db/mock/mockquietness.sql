-- =====================================================================
-- 조용함(소음 측정) 목업 데이터 (MSDS)
--
-- 대상 테이블: quietness_thresholds, quiet_spaces,
--             noise_devices, noise_measurements
--
-- 선행 조건
--   애플리케이션을 한 번 실행해 JPA 테이블을 생성한다.
--
-- 사용법
--   mysql -u <user> -p <database> < mockquietness.sql
--   (또는 DBeaver/Workbench에서 스크립트 전체 실행)
--
-- 특징
--   - 프론트엔드 기본 guesthouse_id인 1에 공간 4개와 측정 기기 4개를 만든다.
--   - 현재 시각 기준 최근 24시간 측정값을 만들어 현황/추천/시간대 차트를 확인한다.
--   - 실행할 때마다 목업 기기의 측정값만 새로 생성해 중복 없이 최신 상태를 유지한다.
--   - 공간명과 기기 serial_number를 자연키로 사용해 반복 실행해도 안전하다.
--   - MySQL 8 / H2(MODE=MySQL) 양쪽에서 동작한다.
--
-- 기대 데이터
--   thresholds 5 / spaces 4 / devices 4 / measurements 32
--   최신값 기준 추천 공간: 마음쉼 명상실
--
-- 되돌리기: mockquietnesscleanup.sql
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. quietness_thresholds
--    기본 5단계가 없을 때만 추가한다. 기존 운영 기준값은 수정하지 않는다.
-- ---------------------------------------------------------------------
INSERT INTO quietness_thresholds (guesthouse_id, level, min_decibel, max_decibel,
                                  display_order, created_at, updated_at)
SELECT 1, t.level, t.min_decibel, t.max_decibel, t.display_order,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT 'VERY_QUIET' AS level,
           CAST(NULL AS DECIMAL(5, 2)) AS min_decibel,
           CAST(29.99 AS DECIMAL(5, 2)) AS max_decibel,
           1 AS display_order
    UNION ALL
    SELECT 'QUIET', 30.00, 39.99, 2
    UNION ALL
    SELECT 'NORMAL', 40.00, 54.99, 3
    UNION ALL
    SELECT 'LOUD', 55.00, 69.99, 4
    UNION ALL
    SELECT 'VERY_LOUD', 70.00, NULL, 5
) AS t
WHERE NOT EXISTS (
    SELECT 1
    FROM quietness_thresholds qt
    WHERE qt.guesthouse_id = 1
      AND qt.level = t.level
);


-- ---------------------------------------------------------------------
-- 2. quiet_spaces
--    type: ROOM / LOUNGE / MEDITATION_ROOM / COMMON_AREA / FACILITY / OTHER
-- ---------------------------------------------------------------------
INSERT INTO quiet_spaces (guesthouse_id, name, type, active, created_at, updated_at)
SELECT 1, t.name, t.type, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '마음쉼 명상실' AS name, 'MEDITATION_ROOM' AS type
    UNION ALL
    SELECT '1층 고요 라운지', 'LOUNGE'
    UNION ALL
    SELECT '소나무 정원', 'COMMON_AREA'
    UNION ALL
    SELECT '오션 테라스', 'FACILITY'
) AS t
WHERE NOT EXISTS (
    SELECT 1
    FROM quiet_spaces qs
    WHERE qs.guesthouse_id = 1
      AND qs.name = t.name
);


-- ---------------------------------------------------------------------
-- 3. noise_devices
--    status: ACTIVE / INACTIVE / DISCONNECTED
-- ---------------------------------------------------------------------
INSERT INTO noise_devices (guesthouse_id, space_id, device_name, serial_number,
                           model_name, status, installed_at, last_connected_at,
                           created_at, updated_at)
SELECT 1, qs.id, t.device_name, t.serial_number,
       'MSDS-SOUND-1', 'ACTIVE',
       TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '마음쉼 명상실' AS space_name,
           '명상실 소음계' AS device_name,
           'MSDS-MOCK-QUIET-01' AS serial_number
    UNION ALL
    SELECT '1층 고요 라운지', '라운지 소음계', 'MSDS-MOCK-QUIET-02'
    UNION ALL
    SELECT '소나무 정원', '정원 소음계', 'MSDS-MOCK-QUIET-03'
    UNION ALL
    SELECT '오션 테라스', '테라스 소음계', 'MSDS-MOCK-QUIET-04'
) AS t
JOIN quiet_spaces qs
  ON qs.guesthouse_id = 1
 AND qs.name = t.space_name
WHERE NOT EXISTS (
    SELECT 1 FROM noise_devices nd WHERE nd.serial_number = t.serial_number
);

UPDATE noise_devices
SET status = 'ACTIVE',
    last_connected_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE serial_number IN (
    'MSDS-MOCK-QUIET-01',
    'MSDS-MOCK-QUIET-02',
    'MSDS-MOCK-QUIET-03',
    'MSDS-MOCK-QUIET-04'
);


-- ---------------------------------------------------------------------
-- 4. noise_measurements
--    목업 기기의 기존 측정값만 비운 뒤 최근 21시간 데이터를 다시 만든다.
-- ---------------------------------------------------------------------
DELETE FROM noise_measurements
WHERE device_id IN (
    SELECT id
    FROM noise_devices
    WHERE serial_number IN (
        'MSDS-MOCK-QUIET-01',
        'MSDS-MOCK-QUIET-02',
        'MSDS-MOCK-QUIET-03',
        'MSDS-MOCK-QUIET-04'
    )
);

INSERT INTO noise_measurements (device_id, guesthouse_id, space_id,
                                decibel, measured_at, created_at)
SELECT nd.id, nd.guesthouse_id, nd.space_id, t.decibel,
       TIMESTAMPADD(HOUR, -t.hours_ago, CURRENT_TIMESTAMP),
       TIMESTAMPADD(HOUR, -t.hours_ago, CURRENT_TIMESTAMP)
FROM (
    -- 마음쉼 명상실: VERY_QUIET ~ QUIET
    SELECT 'MSDS-MOCK-QUIET-01' AS serial_number,
           21 AS hours_ago, CAST(29.20 AS DECIMAL(5, 2)) AS decibel
    UNION ALL SELECT 'MSDS-MOCK-QUIET-01', 18, 31.40
    UNION ALL SELECT 'MSDS-MOCK-QUIET-01', 15, 28.90
    UNION ALL SELECT 'MSDS-MOCK-QUIET-01', 12, 32.10
    UNION ALL SELECT 'MSDS-MOCK-QUIET-01',  9, 30.50
    UNION ALL SELECT 'MSDS-MOCK-QUIET-01',  6, 27.80
    UNION ALL SELECT 'MSDS-MOCK-QUIET-01',  3, 29.70
    UNION ALL SELECT 'MSDS-MOCK-QUIET-01',  0, 28.40

    -- 1층 고요 라운지: NORMAL, 이용객이 많은 시간에는 LOUD
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02', 21, 44.80
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02', 18, 48.20
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02', 15, 53.40
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02', 12, 56.80
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02',  9, 51.30
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02',  6, 49.10
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02',  3, 45.60
    UNION ALL SELECT 'MSDS-MOCK-QUIET-02',  0, 47.50

    -- 소나무 정원: QUIET ~ NORMAL
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03', 21, 36.30
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03', 18, 39.50
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03', 15, 42.70
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03', 12, 48.10
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03',  9, 44.20
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03',  6, 41.60
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03',  3, 37.90
    UNION ALL SELECT 'MSDS-MOCK-QUIET-03',  0, 38.70

    -- 오션 테라스: 바람에 따라 QUIET ~ NORMAL
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04', 21, 33.80
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04', 18, 35.20
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04', 15, 38.60
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04', 12, 41.30
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04',  9, 39.80
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04',  6, 37.10
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04',  3, 36.40
    UNION ALL SELECT 'MSDS-MOCK-QUIET-04',  0, 34.90
) AS t
JOIN noise_devices nd ON nd.serial_number = t.serial_number;
