CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    name          VARCHAR(120) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Every row is owned by a user (data isolation). Ids are client-generated UUIDs so
-- records can be created offline. updated_at = client edit time (conflict resolution),
-- server_modified_at = server write time (sync cursor). deleted = soft-delete tombstone.
CREATE TABLE customers (
    id                 UUID PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name               VARCHAR(200) NOT NULL,
    contact_info       VARCHAR(500),
    updated_at         TIMESTAMPTZ  NOT NULL,
    server_modified_at TIMESTAMPTZ  NOT NULL,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_customers_user_sync ON customers(user_id, server_modified_at);

CREATE TABLE sites (
    id                 UUID PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    customer_id        UUID         NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    name               VARCHAR(200) NOT NULL,
    address            VARCHAR(500),
    updated_at         TIMESTAMPTZ  NOT NULL,
    server_modified_at TIMESTAMPTZ  NOT NULL,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_sites_user_sync ON sites(user_id, server_modified_at);
CREATE INDEX idx_sites_customer  ON sites(customer_id);

CREATE TABLE field_notes (
    id                 UUID PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    site_id            UUID         NOT NULL REFERENCES sites(id) ON DELETE CASCADE,
    title              VARCHAR(200) NOT NULL,
    description        TEXT,
    latitude           DOUBLE PRECISION,
    longitude          DOUBLE PRECISION,
    note_date_time     TIMESTAMPTZ  NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    photo_filename     VARCHAR(100),
    updated_at         TIMESTAMPTZ  NOT NULL,
    server_modified_at TIMESTAMPTZ  NOT NULL,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_notes_user_sync ON field_notes(user_id, server_modified_at);
CREATE INDEX idx_notes_site      ON field_notes(site_id);
CREATE INDEX idx_notes_status    ON field_notes(user_id, status);
