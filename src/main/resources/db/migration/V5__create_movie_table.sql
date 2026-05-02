CREATE TABLE movie (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    classification_id UUID         NOT NULL REFERENCES classification(id),
    title             VARCHAR(255) NOT NULL,
    synopsis          TEXT         NOT NULL,
    duration          INTEGER      NOT NULL CHECK (duration > 0),
    trailer_link      VARCHAR(500),
    original_language VARCHAR(50),
    release_date      DATE         NOT NULL,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);
