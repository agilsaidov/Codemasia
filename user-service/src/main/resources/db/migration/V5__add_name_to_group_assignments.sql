ALTER TABLE group_assignments
    ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT 'Assignment';

CREATE UNIQUE INDEX group_assignments_one_active_per_teacher_idx
    ON group_assignments (group_id, teacher_id)
    WHERE active = TRUE;
