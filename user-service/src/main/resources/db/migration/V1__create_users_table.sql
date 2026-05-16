CREATE TABLE users
(
    id          BIGSERIAL    PRIMARY KEY,
    keycloak_id UUID NOT NULL UNIQUE,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(255) UNIQUE,
    name        VARCHAR(100) NOT NULL,
    surname     VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX users_role_idx ON users (role);
CREATE INDEX users_enabled_idx ON users (enabled);