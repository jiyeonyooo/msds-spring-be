-- =====================================================================
-- 마음 기록(웰니스 체크) 목업 데이터 (MSDS)
--
-- 대상 테이블: wellness_checks, wellness_answers
--
-- 선행 조건
--   1) 애플리케이션을 한 번 실행해 JPA 테이블과 기본 활성 설문/문항을 생성한다.
--   2) mockusers.sql을 먼저 실행한다. 아래 회원 5명을 마음 기록 주인으로 사용한다.
--
-- 사용법
--   mysql -u <user> -p <database> < mockwellness.sql
--   (또는 DBeaver/Workbench에서 스크립트 전체 실행)
--
-- 특징
--   - 목업 회원 5명에게 숙박 전/중/후 기록을 각 1건씩 만든다.
--   - 최근 30일 통계 화면에서 최소 고유 회원 수(기본 5명)를 충족한다.
--   - 실행할 때마다 목업 회원의 기존 마음 기록만 지운 뒤 현재 시각 기준으로
--     다시 생성하므로 반복 실행해도 중복되지 않고 최근 기간에 유지된다.
--   - 점수는 실제 WellnessScoringService 규칙과 같은 0~100 환산값이다.
--   - 설문과 문항은 공용 기준 데이터이므로 생성하거나 수정하지 않는다.
--   - MySQL 8 / H2(MODE=MySQL) 양쪽에서 동작한다.
--
-- 기대 데이터 (활성 문항이 기본 10개일 때)
--   wellness_checks 15 / wellness_answers 150
--
-- 되돌리기: mockwellnesscleanup.sql
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. 재실행 전 목업 회원의 기존 마음 기록 정리
--    mockusers.sql의 계정만 대상으로 하며 다른 회원 기록은 건드리지 않는다.
-- ---------------------------------------------------------------------
DELETE FROM wellness_answers
WHERE wellness_check_id IN (
    SELECT wc.id
    FROM wellness_checks wc
    JOIN users u ON u.id = wc.member_id
    WHERE u.email IN (
        'hong@example.com',
        'minji@example.com',
        'jiyeon@example.com',
        'seojun@example.com',
        'haeun@example.com'
    )
);

DELETE FROM wellness_checks
WHERE member_id IN (
    SELECT id
    FROM users
    WHERE email IN (
        'hong@example.com',
        'minji@example.com',
        'jiyeon@example.com',
        'seojun@example.com',
        'haeun@example.com'
    )
);


-- ---------------------------------------------------------------------
-- 2. wellness_checks
--    stay_stage: GENERAL / BEFORE_STAY / DURING_STAY / AFTER_STAY
--    result_level 경계: 0~20 VERY_RELAXED / 21~40 RELAXED /
--                       41~60 NORMAL / 61~80 TIRED / 81~100 VERY_TIRED
-- ---------------------------------------------------------------------
INSERT INTO wellness_checks (member_id, resv_id, survey_id, stay_stage,
                             total_score, result_level, checked_at, created_at)
SELECT u.id, NULL, s.id, t.stay_stage,
       t.total_score, t.result_level,
       TIMESTAMPADD(DAY, -t.days_ago, CURRENT_TIMESTAMP),
       TIMESTAMPADD(DAY, -t.days_ago, CURRENT_TIMESTAMP)
FROM (
    -- 홍길동: 매우 지침 -> 지침 -> 편안
    SELECT 'hong@example.com' AS user_email,
           'BEFORE_STAY' AS stay_stage,
           100 AS total_score, 'VERY_TIRED' AS result_level, 14 AS days_ago
    UNION ALL
    SELECT 'hong@example.com', 'DURING_STAY', 75, 'TIRED', 10
    UNION ALL
    SELECT 'hong@example.com', 'AFTER_STAY', 25, 'RELAXED', 6

    -- 김민지: 지침 -> 보통 -> 편안
    UNION ALL
    SELECT 'minji@example.com', 'BEFORE_STAY', 75, 'TIRED', 13
    UNION ALL
    SELECT 'minji@example.com', 'DURING_STAY', 50, 'NORMAL', 9
    UNION ALL
    SELECT 'minji@example.com', 'AFTER_STAY', 25, 'RELAXED', 5

    -- 유지연: 지침 -> 보통 -> 매우 편안
    UNION ALL
    SELECT 'jiyeon@example.com', 'BEFORE_STAY', 75, 'TIRED', 12
    UNION ALL
    SELECT 'jiyeon@example.com', 'DURING_STAY', 50, 'NORMAL', 8
    UNION ALL
    SELECT 'jiyeon@example.com', 'AFTER_STAY', 0, 'VERY_RELAXED', 4

    -- 박서준: 보통 -> 편안 -> 편안
    UNION ALL
    SELECT 'seojun@example.com', 'BEFORE_STAY', 50, 'NORMAL', 11
    UNION ALL
    SELECT 'seojun@example.com', 'DURING_STAY', 25, 'RELAXED', 7
    UNION ALL
    SELECT 'seojun@example.com', 'AFTER_STAY', 25, 'RELAXED', 3

    -- 이하은: 매우 지침 -> 지침 -> 보통
    UNION ALL
    SELECT 'haeun@example.com', 'BEFORE_STAY', 100, 'VERY_TIRED', 10
    UNION ALL
    SELECT 'haeun@example.com', 'DURING_STAY', 75, 'TIRED', 6
    UNION ALL
    SELECT 'haeun@example.com', 'AFTER_STAY', 50, 'NORMAL', 2
) AS t
JOIN users u ON u.email = t.user_email
JOIN wellness_surveys s
  ON s.id = (
      SELECT active_survey.id
      FROM wellness_surveys active_survey
      WHERE active_survey.status = 'ACTIVE'
      ORDER BY active_survey.version DESC
      LIMIT 1
  );


-- ---------------------------------------------------------------------
-- 3. wellness_answers
--    각 체크 점수에 해당하는 동일 converted_value(0~4)를 모든 문항에 넣는다.
--    역채점 문항은 answer_value = 4 - converted_value로 저장한다.
-- ---------------------------------------------------------------------
INSERT INTO wellness_answers (wellness_check_id, wellness_question_id,
                              answer_value, converted_value, created_at)
SELECT wc.id,
       wq.id,
       CASE
           WHEN wq.is_reverse_scored THEN 4 - (wc.total_score / 25)
           ELSE wc.total_score / 25
       END AS answer_value,
       wc.total_score / 25 AS converted_value,
       wc.checked_at
FROM wellness_checks wc
JOIN users u ON u.id = wc.member_id
JOIN wellness_questions wq
  ON wq.survey_id = wc.survey_id
 AND wq.status = 'ACTIVE'
WHERE u.email IN (
    'hong@example.com',
    'minji@example.com',
    'jiyeon@example.com',
    'seojun@example.com',
    'haeun@example.com'
)
AND NOT EXISTS (
    SELECT 1
    FROM wellness_answers wa
    WHERE wa.wellness_check_id = wc.id
      AND wa.wellness_question_id = wq.id
);
