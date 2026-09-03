-- =====================================================================
-- 조용함(소음 측정) 목업 데이터 삭제 (mockquietness.sql 되돌리기)
--
-- 삭제 순서: 측정값 -> 기기 -> 공간 (자식부터)
--
-- guesthouse_id=1의 기준값(quietness_thresholds)은 애플리케이션이 공유하는
-- 운영 설정일 수 있으므로 이 cleanup에서는 보존한다.
-- =====================================================================

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

DELETE FROM noise_devices
WHERE serial_number IN (
    'MSDS-MOCK-QUIET-01',
    'MSDS-MOCK-QUIET-02',
    'MSDS-MOCK-QUIET-03',
    'MSDS-MOCK-QUIET-04'
);

DELETE FROM quiet_spaces qs
WHERE qs.guesthouse_id = 1
  AND qs.name IN (
      '마음쉼 명상실',
      '1층 고요 라운지',
      '소나무 정원',
      '오션 테라스'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM noise_devices nd
      WHERE nd.space_id = qs.id
  );
