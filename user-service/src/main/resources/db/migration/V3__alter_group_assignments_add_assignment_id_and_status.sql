ALTER TABLE group_assignments
DROP CONSTRAINT group_assignments_pkey;

ALTER TABLE group_assignments
    ADD COLUMN assignment_id BIGSERIAL NOT NULL,
    ADD COLUMN ends_at       TIMESTAMPTZ,
    ADD COLUMN active        BOOLEAN     NOT NULL DEFAULT TRUE;

ALTER TABLE group_assignments
    ADD CONSTRAINT group_assignments_pkey PRIMARY KEY (assignment_id);

DROP INDEX group_assignments_group_id_idx;
DROP INDEX group_assignments_teacher_id_idx;

CREATE INDEX group_assignments_group_id_active_idx
    ON group_assignments (group_id, active);

CREATE INDEX group_assignments_teacher_id_active_idx
    ON group_assignments (teacher_id, active);

CREATE INDEX group_assignments_group_id_teacher_id_idx
    ON group_assignments (group_id, teacher_id);