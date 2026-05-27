ALTER TABLE groups
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE group_members
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX groups_enabled_idx ON groups (enabled);

CREATE INDEX group_members_group_id_enabled_idx ON group_members (group_id, enabled);
