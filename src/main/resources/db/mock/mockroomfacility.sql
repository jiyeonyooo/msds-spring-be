-- =====================================================================
-- 객실 / 시설 목업 데이터 (MSDS)
--
-- 대상 테이블: rooms, room_units, facilities, room_equipments,
--             room_equipment_mappings
--
-- 사용법
--   mysql -u <user> -p <database> < mock-room-facility.sql
--   (또는 DBeaver/Workbench에서 스크립트 전체 실행)
--
-- 특징
--   - 스프링 부트가 자동 실행하지 않는 위치(db/mock)에 둔다.
--     자동 적재는 classpath 루트의 data.sql만 대상이므로 이 파일은 수동 실행 전용이다.
--   - PK를 직접 지정하지 않고 name 기준으로 조회·삽입하므로
--     AUTO_INCREMENT 값을 건드리지 않고 기존 데이터와 충돌하지 않는다.
--   - 이름이 이미 있으면 건너뛴다(WHERE NOT EXISTS). 여러 번 실행해도 안전하다.
--   - MySQL 8 / H2(MODE=MySQL) 양쪽에서 동작한다.
--
-- 되돌리기: mock-room-facility-cleanup.sql
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. rooms (객실 타입)
--    status: AVAILABLE / SOLDOUT / INAVAILABLE
--    room_type: STAY / REST / MEDITATE / RETREAT
--    bed_type: SINGLE / DOUBLE / QUEEN / KING / TWIN
-- ---------------------------------------------------------------------
INSERT INTO rooms (name, description, main_image_url, room_type, status,
                   standard_guests, max_guests, area_m2, base_price,
                   bed_type, bed_count, created_at, updated_at)
SELECT t.name, t.description, t.main_image_url, t.room_type, t.status,
       t.standard_guests, t.max_guests, t.area_m2, t.base_price,
       t.bed_type, t.bed_count, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '오션 사일런스 스위트' AS name,
           '바다와 하늘이 가장 고요하게 만나는 자리. 통창 너머로 수평선이 이어지는 스위트룸.' AS description,
           'https://cdn.msds.example.com/rooms/ocean-silence-suite.jpg' AS main_image_url,
           'STAY' AS room_type, 'AVAILABLE' AS status,
           2 AS standard_guests, 3 AS max_guests, 38.00 AS area_m2, 180000 AS base_price,
           'KING' AS bed_type, 1 AS bed_count
    UNION ALL
    SELECT '포레스트 트윈', '숲의 결을 따라 천천히 쉬어가는 트윈룸. 창을 열면 잣나무 향이 들어온다.',
           'https://cdn.msds.example.com/rooms/forest-twin.jpg',
           'REST', 'AVAILABLE', 2, 2, 32.00, 140000, 'TWIN', 2
    UNION ALL
    SELECT '스틸 싱글룸', '정원을 바라보며 혼자 머무는 1인 명상 객실. 좌식 명상 공간이 따로 있다.',
           'https://cdn.msds.example.com/rooms/still-single.jpg',
           'MEDITATE', 'AVAILABLE', 1, 1, 24.00, 110000, 'SINGLE', 1
    UNION ALL
    SELECT '리트릿 하우스', '2박 이상 머무는 리트릿 전용 독채. 거실과 다실이 분리되어 있다.',
           'https://cdn.msds.example.com/rooms/retreat-house.jpg',
           'RETREAT', 'AVAILABLE', 4, 6, 62.50, 320000, 'QUEEN', 2
    UNION ALL
    SELECT '가든 더블룸', '내정원과 이어지는 1층 더블룸. 아침 산책로로 바로 나갈 수 있다.',
           'https://cdn.msds.example.com/rooms/garden-double.jpg',
           'STAY', 'SOLDOUT', 2, 2, 28.00, 150000, 'DOUBLE', 1
    UNION ALL
    SELECT '문라이트 로프트', '복층 구조의 로프트. 현재 리모델링으로 예약을 받지 않는다.',
           'https://cdn.msds.example.com/rooms/moonlight-loft.jpg',
           'REST', 'INAVAILABLE', 2, 4, 45.00, 210000, 'QUEEN', 1
) AS t
WHERE NOT EXISTS (SELECT 1 FROM rooms r WHERE r.name = t.name);


-- ---------------------------------------------------------------------
-- 2. room_units (실제 호실)
--    status: ACTIVE / MAINTENANCE / INACTIVE
--    (room_id, room_number)에 유니크 제약이 있어 중복 삽입되지 않는다.
--    예약 가능 조회는 ACTIVE 호실만 센다.
-- ---------------------------------------------------------------------
INSERT INTO room_units (room_id, room_number, floor, status, created_at, updated_at)
SELECT r.id, t.room_number, t.floor_no, t.status, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '오션 사일런스 스위트' AS room_name, '201' AS room_number, 2 AS floor_no, 'ACTIVE' AS status
    UNION ALL SELECT '오션 사일런스 스위트', '202', 2, 'ACTIVE'
    UNION ALL SELECT '오션 사일런스 스위트', '203', 2, 'MAINTENANCE'
    UNION ALL SELECT '포레스트 트윈',        '301', 3, 'ACTIVE'
    UNION ALL SELECT '포레스트 트윈',        '302', 3, 'ACTIVE'
    UNION ALL SELECT '스틸 싱글룸',          '101', 1, 'ACTIVE'
    UNION ALL SELECT '스틸 싱글룸',          '102', 1, 'ACTIVE'
    UNION ALL SELECT '리트릿 하우스',        '401', 4, 'ACTIVE'
    UNION ALL SELECT '가든 더블룸',          '103', 1, 'ACTIVE'
    UNION ALL SELECT '가든 더블룸',          '104', 1, 'ACTIVE'
    UNION ALL SELECT '문라이트 로프트',      '501', 5, 'INACTIVE'
    UNION ALL SELECT '문라이트 로프트',      '502', 5, 'MAINTENANCE'
) AS t
JOIN rooms r ON r.name = t.room_name
WHERE NOT EXISTS (
    SELECT 1 FROM room_units u WHERE u.room_id = r.id AND u.room_number = t.room_number
);


-- ---------------------------------------------------------------------
-- 3. facilities (부대시설)
--    category: WELLNESS / LEISURE / FOOD / BUSINESS / CONVENIENCE /
--              PARKING / ACCESSIBILITY / ETC
--    name에 유니크 제약이 있다. 공개 조회 API는 active = TRUE만 노출한다.
-- ---------------------------------------------------------------------
INSERT INTO facilities (name, category, description, image_url, active, created_at, updated_at)
SELECT t.name, t.category, t.description, t.image_url, t.active,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '마음쉼 명상실' AS name, 'WELLNESS' AS category,
           '호흡과 명상 프로그램을 진행하는 고요한 공간' AS description,
           'https://cdn.msds.example.com/facilities/meditation-room.jpg' AS image_url,
           TRUE AS active
    UNION ALL
    SELECT '사운드 배스 스튜디오', 'WELLNESS', '싱잉볼 사운드 배스를 체험하는 방음 스튜디오',
           'https://cdn.msds.example.com/facilities/sound-bath.jpg', TRUE
    UNION ALL
    SELECT '정원 산책로', 'LEISURE', '천천히 걸으며 계절의 흐름을 느끼는 600m 산책로',
           'https://cdn.msds.example.com/facilities/garden-trail.jpg', TRUE
    UNION ALL
    SELECT '루프탑 스타게이징 데크', 'LEISURE', '밤하늘을 바라보며 머무는 옥상 데크',
           'https://cdn.msds.example.com/facilities/rooftop-deck.jpg', TRUE
    UNION ALL
    SELECT '티 라운지', 'FOOD', '따뜻한 차와 함께 머무는 공용 라운지',
           'https://cdn.msds.example.com/facilities/tea-lounge.jpg', TRUE
    UNION ALL
    SELECT '조식 다이닝룸', 'FOOD', '제철 재료로 차리는 사찰식 조식 공간',
           'https://cdn.msds.example.com/facilities/dining-room.jpg', TRUE
    UNION ALL
    SELECT '스몰 미팅룸', 'BUSINESS', '8인 규모의 워크숍·회의 공간',
           'https://cdn.msds.example.com/facilities/meeting-room.jpg', TRUE
    UNION ALL
    SELECT '공용 주방', 'CONVENIENCE', '간단한 식사와 음료를 준비할 수 있는 주방',
           'https://cdn.msds.example.com/facilities/shared-kitchen.jpg', TRUE
    UNION ALL
    SELECT '전용 주차장', 'PARKING', '투숙객이 이용하는 무료 주차 공간 20면',
           'https://cdn.msds.example.com/facilities/parking.jpg', TRUE
    UNION ALL
    SELECT '무장애 경사로', 'ACCESSIBILITY', '휠체어 접근이 가능한 전 층 경사로와 엘리베이터',
           'https://cdn.msds.example.com/facilities/accessible-ramp.jpg', TRUE
    UNION ALL
    -- 비활성 시설: 공개 목록에서 빠지는지 확인하는 용도
    SELECT '코인 세탁실', 'ETC', '설비 교체로 임시 운영 중단',
           'https://cdn.msds.example.com/facilities/laundry.jpg', FALSE
) AS t
WHERE NOT EXISTS (SELECT 1 FROM facilities f WHERE f.name = t.name);


-- ---------------------------------------------------------------------
-- 4. room_equipments (객실 비품 마스터)
--    category: ELECTRONICS / FURNITURE / BATHROOM / BEDDING /
--              KITCHEN / CONVENIENCE / WELLNESS
--    name에 유니크 제약이 있다. 비품 옵션 조회는 active = TRUE만 노출한다.
-- ---------------------------------------------------------------------
INSERT INTO room_equipments (name, category, description, icon_url, active, created_at, updated_at)
SELECT t.name, t.category, t.description, t.icon_url, t.active,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '무선 인터넷' AS name, 'CONVENIENCE' AS category,
           '객실 전용 Wi-Fi' AS description,
           'https://cdn.msds.example.com/icons/wifi.svg' AS icon_url,
           TRUE AS active
    UNION ALL SELECT '개별 냉난방기', 'ELECTRONICS', '객실별 온도 조절', 'https://cdn.msds.example.com/icons/hvac.svg', TRUE
    UNION ALL SELECT '스마트 TV',    'ELECTRONICS', '55인치 스마트 TV',  'https://cdn.msds.example.com/icons/tv.svg', TRUE
    UNION ALL SELECT '공기청정기',   'ELECTRONICS', '객실 공기질 관리',  'https://cdn.msds.example.com/icons/air-purifier.svg', TRUE
    UNION ALL SELECT '원목 책상',    'FURNITURE',   '기록과 작업을 위한 책상', 'https://cdn.msds.example.com/icons/desk.svg', TRUE
    UNION ALL SELECT '2인 소파',     'FURNITURE',   '창가 휴식용 소파',  'https://cdn.msds.example.com/icons/sofa.svg', TRUE
    UNION ALL SELECT '레인 샤워',    'BATHROOM',    '레인 샤워 헤드',    'https://cdn.msds.example.com/icons/shower.svg', TRUE
    UNION ALL SELECT '욕조',         'BATHROOM',    '반신욕이 가능한 욕조', 'https://cdn.msds.example.com/icons/bathtub.svg', TRUE
    UNION ALL SELECT '호텔 침구',    'BEDDING',     '편안한 숙면을 위한 구스 침구', 'https://cdn.msds.example.com/icons/bedding.svg', TRUE
    UNION ALL SELECT '차 세트',      'KITCHEN',     '차와 온수 포트',    'https://cdn.msds.example.com/icons/tea-set.svg', TRUE
    UNION ALL SELECT '미니 냉장고',  'KITCHEN',     '객실 내 소형 냉장고', 'https://cdn.msds.example.com/icons/fridge.svg', TRUE
    UNION ALL SELECT '명상 쿠션',    'WELLNESS',    '객실 내 명상용 방석', 'https://cdn.msds.example.com/icons/cushion.svg', TRUE
    -- 비활성 비품: 옵션 목록에서 빠지는지 확인하는 용도
    UNION ALL SELECT '아로마 디퓨저', 'WELLNESS',   '향 알레르기 이슈로 운영 중단', 'https://cdn.msds.example.com/icons/diffuser.svg', FALSE
) AS t
WHERE NOT EXISTS (SELECT 1 FROM room_equipments e WHERE e.name = t.name);


-- ---------------------------------------------------------------------
-- 5. room_equipment_mappings (객실 ↔ 비품 매핑)
--    (room_id, equipment_id)에 유니크 제약이 있다.
-- ---------------------------------------------------------------------
INSERT INTO room_equipment_mappings (room_id, equipment_id, quantity, note, created_at, updated_at)
SELECT r.id, e.id, t.quantity, t.note, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT '오션 사일런스 스위트' AS room_name, '무선 인터넷' AS equipment_name, 1 AS quantity, '기가 와이파이' AS note
    UNION ALL SELECT '오션 사일런스 스위트', '개별 냉난방기', 1, '거실·침실 개별 제어'
    UNION ALL SELECT '오션 사일런스 스위트', '스마트 TV',    1, '오션뷰 창가 배치'
    UNION ALL SELECT '오션 사일런스 스위트', '욕조',         1, '오션뷰 욕조'
    UNION ALL SELECT '오션 사일런스 스위트', '호텔 침구',    1, '킹 사이즈'
    UNION ALL SELECT '오션 사일런스 스위트', '차 세트',      1, '지역 차 3종'
    UNION ALL SELECT '오션 사일런스 스위트', '미니 냉장고',  1, '웰컴 음료 포함'

    UNION ALL SELECT '포레스트 트윈', '무선 인터넷',   1, NULL
    UNION ALL SELECT '포레스트 트윈', '개별 냉난방기', 1, NULL
    UNION ALL SELECT '포레스트 트윈', '공기청정기',    1, NULL
    UNION ALL SELECT '포레스트 트윈', '호텔 침구',     2, '싱글 2개'
    UNION ALL SELECT '포레스트 트윈', '레인 샤워',     1, NULL
    UNION ALL SELECT '포레스트 트윈', '차 세트',       1, NULL

    UNION ALL SELECT '스틸 싱글룸', '무선 인터넷',  1, NULL
    UNION ALL SELECT '스틸 싱글룸', '개별 냉난방기', 1, NULL
    UNION ALL SELECT '스틸 싱글룸', '명상 쿠션',    2, '좌식 명상 공간용'
    UNION ALL SELECT '스틸 싱글룸', '원목 책상',    1, '기록용 데스크'
    UNION ALL SELECT '스틸 싱글룸', '호텔 침구',    1, NULL

    UNION ALL SELECT '리트릿 하우스', '무선 인터넷', 1, NULL
    UNION ALL SELECT '리트릿 하우스', '스마트 TV',   1, '거실 배치'
    UNION ALL SELECT '리트릿 하우스', '2인 소파',    2, '거실·다실 각 1개'
    UNION ALL SELECT '리트릿 하우스', '명상 쿠션',   6, '단체 명상용'
    UNION ALL SELECT '리트릿 하우스', '호텔 침구',   2, '퀸 2개'
    UNION ALL SELECT '리트릿 하우스', '미니 냉장고', 1, NULL

    UNION ALL SELECT '가든 더블룸', '무선 인터넷',  1, NULL
    UNION ALL SELECT '가든 더블룸', '개별 냉난방기', 1, NULL
    UNION ALL SELECT '가든 더블룸', '호텔 침구',    1, '더블 사이즈'
    UNION ALL SELECT '가든 더블룸', '레인 샤워',    1, NULL

    UNION ALL SELECT '문라이트 로프트', '무선 인터넷', 1, NULL
    UNION ALL SELECT '문라이트 로프트', '호텔 침구',   1, '리모델링 후 교체 예정'
) AS t
JOIN rooms r ON r.name = t.room_name
JOIN room_equipments e ON e.name = t.equipment_name
WHERE NOT EXISTS (
    SELECT 1 FROM room_equipment_mappings m WHERE m.room_id = r.id AND m.equipment_id = e.id
);
