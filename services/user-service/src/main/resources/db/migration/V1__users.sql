CREATE TABLE app_user (
                          id            BIGSERIAL PRIMARY KEY,
                          email         VARCHAR(200) NOT NULL UNIQUE,
                          display_name  VARCHAR(100) NOT NULL,
                          password_hash VARCHAR(100) NOT NULL,
                          created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);