CREATE TABLE groups
(
    id          VARCHAR(20)    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_by  BIGINT       NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE group_members
(
    group_id  VARCHAR(20)      NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    user_id   BIGINT      NOT NULL REFERENCES users (id)  ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, user_id)
);

CREATE TABLE group_assignments
(
    group_id    VARCHAR(20)      NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    teacher_id  BIGINT      NOT NULL REFERENCES users (id)  ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, teacher_id)
);

-- groups indexes
CREATE INDEX groups_created_by_idx ON groups (created_by);

-- group_members indexes
CREATE INDEX group_members_group_id_idx  ON group_members (group_id);
CREATE INDEX group_members_user_id_idx   ON group_members (user_id);

-- group_assignments indexes
CREATE INDEX group_assignments_group_id_idx   ON group_assignments (group_id);
CREATE INDEX group_assignments_teacher_id_idx ON group_assignments (teacher_id);
