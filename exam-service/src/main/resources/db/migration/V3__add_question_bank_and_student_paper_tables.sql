-- Optional difficulty tag on bank problems (NULL = not classified)
ALTER TABLE problems
    ADD COLUMN difficulty VARCHAR(10);

ALTER TABLE problems
    ADD CONSTRAINT problems_difficulty_chk
        CHECK (difficulty IS NULL OR difficulty IN ('EASY', 'MEDIUM', 'HARD'));

CREATE INDEX problems_exam_id_difficulty_idx ON problems (exam_id, difficulty);

-- Session rules (quotas, reshuffle, proctoring, selection mode)
ALTER TABLE exam_sessions
    ADD COLUMN selection_mode        VARCHAR(10)  NOT NULL DEFAULT 'RANDOM',
    ADD COLUMN use_difficulty_tiers  BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN question_quota        INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN easy_quota            INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN medium_quota          INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN hard_quota            INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN max_question_changes  INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN max_cheat_events      INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN cheat_block_mode      VARCHAR(20)  NOT NULL DEFAULT 'SUBMIT_BLOCKED';

ALTER TABLE exam_sessions
    ADD CONSTRAINT exam_sessions_selection_mode_chk
        CHECK (selection_mode IN ('FIXED', 'RANDOM'));

ALTER TABLE exam_sessions
    ADD CONSTRAINT exam_sessions_cheat_block_mode_chk
        CHECK (cheat_block_mode IN ('SUBMIT_BLOCKED', 'FULL_BLOCKED'));

ALTER TABLE exam_sessions
    ADD CONSTRAINT exam_sessions_quotas_non_negative_chk
        CHECK (question_quota >= 0
            AND easy_quota >= 0
            AND medium_quota >= 0
            AND hard_quota >= 0
            AND max_question_changes >= 0
            AND max_cheat_events >= 0);

-- RANDOM + tiers: use easy/medium/hard quotas (problems must be tagged).
-- RANDOM + no tiers: use question_quota from the whole bank (difficulty optional).
-- FIXED: same paper for everyone (quotas ignored; enforced in application layer).

-- Allowed programming languages per session (same exam, different groups / PLs)
CREATE TABLE exam_session_languages
(
    session_id BIGINT       NOT NULL REFERENCES exam_sessions (id) ON DELETE CASCADE,
    language   VARCHAR(20)  NOT NULL,
    PRIMARY KEY (session_id, language)
);

CREATE INDEX exam_session_languages_session_id_idx ON exam_session_languages (session_id);

-- Per-student session state (reshuffles, cheats, block, paper version)
CREATE TABLE student_exam_states
(
    id            BIGSERIAL   PRIMARY KEY,
    session_id    BIGINT      NOT NULL REFERENCES exam_sessions (id) ON DELETE CASCADE,
    student_id    UUID        NOT NULL,
    change_count  INTEGER     NOT NULL DEFAULT 0,
    cheat_count   INTEGER     NOT NULL DEFAULT 0,
    has_submitted BOOLEAN     NOT NULL DEFAULT FALSE,
    blocked       BOOLEAN     NOT NULL DEFAULT FALSE,
    block_reason  VARCHAR(150),
    blocked_at    TIMESTAMPTZ,
    paper_version INTEGER     NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT student_exam_states_session_student_uq UNIQUE (session_id, student_id),
    CONSTRAINT student_exam_states_change_count_non_negative_chk CHECK (change_count >= 0),
    CONSTRAINT student_exam_states_cheat_count_non_negative_chk CHECK (cheat_count >= 0),
    CONSTRAINT student_exam_states_paper_version_positive_chk CHECK (paper_version >= 1)
);

CREATE INDEX student_exam_states_session_id_idx ON student_exam_states (session_id);
CREATE INDEX student_exam_states_student_id_idx ON student_exam_states (student_id);
CREATE INDEX student_exam_states_session_id_blocked_idx ON student_exam_states (session_id, blocked);

-- Assigned problems (versioned student paper, kept for audit)
CREATE TABLE student_exam_problems
(
    id                    BIGSERIAL   PRIMARY KEY,
    student_exam_state_id BIGINT      NOT NULL REFERENCES student_exam_states (id) ON DELETE CASCADE,
    problem_id            BIGINT      NOT NULL REFERENCES problems (id) ON DELETE RESTRICT,
    difficulty            VARCHAR(10),
    position              INTEGER     NOT NULL,
    paper_version         INTEGER     NOT NULL,
    active                BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT student_exam_problems_difficulty_chk
        CHECK (difficulty IS NULL OR difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT student_exam_problems_position_non_negative_chk
        CHECK (position >= 0),
    CONSTRAINT student_exam_problems_paper_version_positive_chk
        CHECK (paper_version >= 1),
    CONSTRAINT student_exam_problems_state_problem_version_uq
        UNIQUE (student_exam_state_id, problem_id, paper_version)
);

CREATE INDEX student_exam_problems_state_id_idx
    ON student_exam_problems (student_exam_state_id);

CREATE INDEX student_exam_problems_state_id_active_idx
    ON student_exam_problems (student_exam_state_id, active);

CREATE INDEX student_exam_problems_state_id_paper_version_idx
    ON student_exam_problems (student_exam_state_id, paper_version);
