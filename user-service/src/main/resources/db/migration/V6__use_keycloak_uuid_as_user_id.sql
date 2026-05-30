-- ============================================================
-- Switch user identity from a BIGSERIAL surrogate id + separate
-- keycloak_id column to using the Keycloak UUID directly as
-- users.id. All FK references (groups.created_by,
-- group_members.user_id, group_assignments.teacher_id) are
-- remapped from the old BIGINT id to the Keycloak UUID.
-- Existing rows are preserved via the users.keycloak_id mapping.
-- ============================================================

-- 1) Drop the FK constraints that point at users(id).
ALTER TABLE groups            DROP CONSTRAINT groups_created_by_fkey;
ALTER TABLE group_members     DROP CONSTRAINT group_members_user_id_fkey;
ALTER TABLE group_assignments DROP CONSTRAINT group_assignments_teacher_id_fkey;

-- 2) Add UUID columns on child tables and backfill them from
--    users.keycloak_id while the old BIGINT id still exists.
ALTER TABLE groups            ADD COLUMN created_by_uuid UUID;
ALTER TABLE group_members     ADD COLUMN user_id_uuid    UUID;
ALTER TABLE group_assignments ADD COLUMN teacher_id_uuid UUID;

UPDATE groups g
   SET created_by_uuid = u.keycloak_id
  FROM users u
 WHERE u.id = g.created_by;

UPDATE group_members gm
   SET user_id_uuid = u.keycloak_id
  FROM users u
 WHERE u.id = gm.user_id;

UPDATE group_assignments ga
   SET teacher_id_uuid = u.keycloak_id
  FROM users u
 WHERE u.id = ga.teacher_id;

-- 3) Rebuild the users table so the Keycloak UUID becomes the PK.
ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users DROP CONSTRAINT users_keycloak_id_key;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN keycloak_id TO id;
ALTER TABLE users ADD PRIMARY KEY (id);

-- 4) Swap the child FK columns over to the new UUID columns,
--    restore NOT NULL, FK constraints and indexes.

-- groups.created_by
ALTER TABLE groups DROP COLUMN created_by;
ALTER TABLE groups RENAME COLUMN created_by_uuid TO created_by;
ALTER TABLE groups ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE groups ADD CONSTRAINT groups_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT;
CREATE INDEX groups_created_by_idx ON groups (created_by);

-- group_members.user_id (part of the composite primary key)
ALTER TABLE group_members DROP CONSTRAINT group_members_pkey;
ALTER TABLE group_members DROP COLUMN user_id;
ALTER TABLE group_members RENAME COLUMN user_id_uuid TO user_id;
ALTER TABLE group_members ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE group_members ADD CONSTRAINT group_members_pkey PRIMARY KEY (group_id, user_id);
ALTER TABLE group_members ADD CONSTRAINT group_members_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
CREATE INDEX group_members_user_id_idx ON group_members (user_id);

-- group_assignments.teacher_id (PK is assignment_id since V3)
ALTER TABLE group_assignments DROP COLUMN teacher_id;
ALTER TABLE group_assignments RENAME COLUMN teacher_id_uuid TO teacher_id;
ALTER TABLE group_assignments ALTER COLUMN teacher_id SET NOT NULL;
ALTER TABLE group_assignments ADD CONSTRAINT group_assignments_teacher_id_fkey
    FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE CASCADE;
CREATE INDEX group_assignments_teacher_id_active_idx
    ON group_assignments (teacher_id, active);
CREATE INDEX group_assignments_group_id_teacher_id_idx
    ON group_assignments (group_id, teacher_id);
CREATE UNIQUE INDEX group_assignments_one_active_per_teacher_idx
    ON group_assignments (group_id, teacher_id)
    WHERE active = TRUE;
