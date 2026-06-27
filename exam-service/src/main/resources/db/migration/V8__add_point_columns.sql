ALTER TABLE exam_sessions
    ADD COLUMN total_exam_points NUMERIC(10,2) NOT NULL DEFAULT 50,
    ADD COLUMN question_quota_points NUMERIC(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN easy_quota_points NUMERIC(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN medium_quota_points NUMERIC(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN hard_quota_points NUMERIC(10,2) NOT NULL DEFAULT 0;

ALTER TABLE problems
DROP COLUMN points;
