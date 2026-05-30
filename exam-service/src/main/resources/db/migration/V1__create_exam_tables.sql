
CREATE TABLE exams
(
    id            VARCHAR(20)   PRIMARY KEY,
    title         VARCHAR(200)  NOT NULL,
    description   VARCHAR(2000),
    created_by    UUID          NOT NULL,
    publish_ready BOOLEAN       NOT NULL DEFAULT FALSE,
    enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE problems
(
    id              BIGSERIAL    PRIMARY KEY,
    exam_id         VARCHAR(20)  NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    statement       TEXT         NOT NULL,
    time_limit_ms   INTEGER      NOT NULL DEFAULT 1000,
    memory_limit_kb INTEGER      NOT NULL DEFAULT 128000,
    points          INTEGER      NOT NULL DEFAULT 100,
    position        INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE test_cases
(
    id              BIGSERIAL   PRIMARY KEY,
    problem_id      BIGINT      NOT NULL REFERENCES problems (id) ON DELETE CASCADE,
    stdin           TEXT        NOT NULL DEFAULT '',
    expected_output TEXT        NOT NULL,
    is_sample       BOOLEAN     NOT NULL DEFAULT FALSE,
    weight          INTEGER     NOT NULL DEFAULT 1,
    position        INTEGER     NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE exam_sessions
(
    id               BIGSERIAL   PRIMARY KEY,
    exam_id          VARCHAR(20) NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    group_id         VARCHAR(20) NOT NULL,
    assigned_by      UUID        NOT NULL,
    starts_at        TIMESTAMPTZ,
    ends_at          TIMESTAMPTZ,
    enabled          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- exams indexes
CREATE INDEX exams_created_by_idx    ON exams (created_by);
CREATE INDEX exams_publish_ready_idx ON exams (publish_ready);
CREATE INDEX exams_enabled_idx       ON exams (enabled);

-- problems indexes
CREATE INDEX problems_exam_id_idx ON problems (exam_id);

-- test_cases indexes
CREATE INDEX test_cases_problem_id_idx           ON test_cases (problem_id);
CREATE INDEX test_cases_problem_id_is_sample_idx ON test_cases (problem_id, is_sample);

-- exam_sessions indexes
CREATE INDEX exam_sessions_exam_id_idx        ON exam_sessions (exam_id);
CREATE INDEX exam_sessions_group_id_idx       ON exam_sessions (group_id);
CREATE INDEX exam_sessions_group_id_starts_at_idx ON exam_sessions (group_id, starts_at);
