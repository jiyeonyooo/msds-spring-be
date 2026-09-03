-- =====================================================================
-- 객실 / 시설 목업 데이터 삭제 (mock-room-facility.sql 되돌리기)
--
-- 주의
--   resv.room_units_id는 FK 없이 값만 들고 있다. 목업 호실로 잡힌 예약이
--   남아 있으면 삭제 후 그 예약이 존재하지 않는 호실을 가리키게 된다.
--   아래 확인 쿼리가 0건인지 먼저 보고 실행할 것.
--
--   SELECT COUNT(*) FROM resv
--    WHERE room_units_id IN (
--          SELECT u.id FROM room_units u
--            JOIN rooms r ON r.id = u.room_id
--           WHERE r.name IN ('오션 사일런스 스위트', '포레스트 트윈', '스틸 싱글룸',
--                            '리트릿 하우스', '가든 더블룸', '문라이트 로프트'));
--
-- 삭제 순서: 매핑 -> 호실 -> 객실 -> 시설/비품 (자식부터)
-- =====================================================================

DELETE FROM room_equipment_mappings
WHERE room_id IN (
    SELECT id FROM rooms
    WHERE name IN ('오션 사일런스 스위트', '포레스트 트윈', '스틸 싱글룸',
                   '리트릿 하우스', '가든 더블룸', '문라이트 로프트')
);

DELETE FROM room_units
WHERE room_id IN (
    SELECT id FROM rooms
    WHERE name IN ('오션 사일런스 스위트', '포레스트 트윈', '스틸 싱글룸',
                   '리트릿 하우스', '가든 더블룸', '문라이트 로프트')
);

DELETE FROM rooms
WHERE name IN ('오션 사일런스 스위트', '포레스트 트윈', '스틸 싱글룸',
               '리트릿 하우스', '가든 더블룸', '문라이트 로프트');

DELETE FROM facilities
WHERE name IN ('마음쉼 명상실', '사운드 배스 스튜디오', '정원 산책로',
               '루프탑 스타게이징 데크', '티 라운지', '조식 다이닝룸',
               '스몰 미팅룸', '공용 주방', '전용 주차장',
               '무장애 경사로', '코인 세탁실');

DELETE FROM room_equipments
WHERE name IN ('무선 인터넷', '개별 냉난방기', '스마트 TV', '공기청정기',
               '원목 책상', '2인 소파', '레인 샤워', '욕조', '호텔 침구',
               '차 세트', '미니 냉장고', '명상 쿠션', '아로마 디퓨저');
