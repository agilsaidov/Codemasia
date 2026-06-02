ALTER TABLE exam_sessions
    ADD COLUMN status     VARCHAR(10)  NOT NULL DEFAULT 'SCHEDULED',
    ADD COLUMN exam_title VARCHAR(200) NOT NULL DEFAULT '';

ALTER TABLE exam_sessions
    ADD CONSTRAINT exam_sessions_status_chk
        CHECK (status IN ('SCHEDULED', 'ACTIVE', 'CLOSING', 'FINISHED', 'CANCELLED'));

UPDATE exam_sessions es
SET exam_title = e.title
FROM exams e
WHERE es.exam_id = e.id;

UPDATE exam_sessions
SET status = 'CANCELLED'
WHERE enabled = FALSE;

UPDATE exam_sessions
SET status = 'FINISHED'
WHERE enabled = TRUE
  AND ends_at IS NOT NULL
  AND ends_at <= now();

UPDATE exam_sessions
SET status = 'SCHEDULED'
WHERE enabled = TRUE
  AND status = 'SCHEDULED'
  AND starts_at IS NOT NULL
  AND starts_at > now();

UPDATE exam_sessions
SET status = 'ACTIVE'
WHERE enabled = TRUE
  AND status = 'SCHEDULED';

CREATE INDEX exam_sessions_exam_id_status_idx ON exam_sessions (exam_id, status);
