-- =====================================================================
-- noise_measurements mock data for guesthouse 1
--
-- Prerequisite:
--   noise_devices must contain at least one device for guesthouse_id = 1.
--
-- This script replaces measurements from the last 24 hours for guesthouse 1
-- and creates eight readings per registered device. It is intended for local
-- development only.
-- =====================================================================

-- Make repeated local runs predictable without touching older history.
DELETE FROM noise_measurements
WHERE guesthouse_id = 1
  AND measured_at >= TIMESTAMPADD(HOUR, -24, CURRENT_TIMESTAMP)
  -- id is the primary key; this condition also satisfies MySQL safe-update mode.
  AND id > 0;

INSERT INTO noise_measurements (
    device_id,
    guesthouse_id,
    space_id,
    decibel,
    measured_at,
    created_at
)
SELECT
    device.id,
    device.guesthouse_id,
    device.space_id,
    CAST(
        sample.base_decibel + MOD(device.id, 4) * 1.25
        AS DECIMAL(5, 2)
    ) AS decibel,
    TIMESTAMPADD(HOUR, -sample.hours_ago, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP
FROM noise_devices AS device
CROSS JOIN (
    SELECT 21 AS hours_ago, CAST(31.20 AS DECIMAL(5, 2)) AS base_decibel
    UNION ALL SELECT 18, 34.50
    UNION ALL SELECT 15, 38.10
    UNION ALL SELECT 12, 43.70
    UNION ALL SELECT  9, 47.20
    UNION ALL SELECT  6, 41.80
    UNION ALL SELECT  3, 36.40
    UNION ALL SELECT  0, 33.60
) AS sample
WHERE device.guesthouse_id = 1;

-- Verification: row_count must be greater than zero.
SELECT
    guesthouse_id,
    COUNT(*) AS row_count,
    COUNT(DISTINCT space_id) AS measured_space_count,
    MIN(measured_at) AS first_measured_at,
    MAX(measured_at) AS latest_measured_at
FROM noise_measurements
WHERE guesthouse_id = 1
GROUP BY guesthouse_id;
