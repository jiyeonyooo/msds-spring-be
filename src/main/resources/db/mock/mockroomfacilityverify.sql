-- =====================================================================
-- 목업 데이터 적재 결과 확인 쿼리
-- mock-room-facility.sql 실행 후 값이 기대와 맞는지 본다.
-- =====================================================================

-- 1) 테이블별 건수 (목업만 새로 넣은 DB 기준 기대값)
--    rooms 6 / room_units 12 / facilities 11 / room_equipments 13 / mappings 30
SELECT 'rooms' AS table_name, COUNT(*) AS row_count FROM rooms
UNION ALL SELECT 'room_units',              COUNT(*) FROM room_units
UNION ALL SELECT 'facilities',              COUNT(*) FROM facilities
UNION ALL SELECT 'room_equipments',         COUNT(*) FROM room_equipments
UNION ALL SELECT 'room_equipment_mappings', COUNT(*) FROM room_equipment_mappings;

-- 2) 객실별 호실 수 / 비품 수 / 예약 가능(ACTIVE) 호실 수
SELECT r.id,
       r.name,
       r.room_type,
       r.status,
       r.max_guests,
       r.base_price,
       COUNT(DISTINCT u.id)                                          AS unit_count,
       COUNT(DISTINCT CASE WHEN u.status = 'ACTIVE' THEN u.id END)   AS active_unit_count,
       COUNT(DISTINCT m.equipment_id)                                AS equipment_count
FROM rooms r
LEFT JOIN room_units u ON u.room_id = r.id
LEFT JOIN room_equipment_mappings m ON m.room_id = r.id
GROUP BY r.id, r.name, r.room_type, r.status, r.max_guests, r.base_price
ORDER BY r.id;

-- 3) 시설 카테고리별 노출 건수 (공개 API는 active = TRUE만 내려준다)
SELECT category,
       COUNT(*)                                  AS total,
       SUM(CASE WHEN active THEN 1 ELSE 0 END)   AS active_count
FROM facilities
GROUP BY category
ORDER BY category;

-- 4) 비품 카테고리별 노출 건수
SELECT category,
       COUNT(*)                                  AS total,
       SUM(CASE WHEN active THEN 1 ELSE 0 END)   AS active_count
FROM room_equipments
GROUP BY category
ORDER BY category;

-- 5) 예약 가능 조회가 실제로 쓰는 형태
--    (ACTIVE 호실 중, 요청 기간과 겹치는 RESERVED 예약이 없는 호실 수)
--    :checkInDate / :checkOutDate 자리에 날짜를 넣어 실행한다.
SELECT r.id,
       r.name,
       COUNT(u.id) AS remaining_count
FROM rooms r
LEFT JOIN room_units u
       ON u.room_id = r.id
      AND u.status = 'ACTIVE'
      AND NOT EXISTS (
          SELECT 1 FROM resv v
          WHERE v.room_units_id = u.id
            AND v.resv_status = 'RESERVED'
            AND v.check_in_date < '2026-10-05'   -- checkOutDate
            AND v.check_out_date > '2026-10-02'  -- checkInDate
      )
WHERE r.max_guests >= 2                          -- guestCount
GROUP BY r.id, r.name
ORDER BY r.id;
