CREATE TABLE classification (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    country_id  UUID        NOT NULL REFERENCES country(id),
    name        VARCHAR(20) NOT NULL,
    age_limit   INTEGER     NOT NULL CHECK (age_limit >= 0),
    UNIQUE (country_id, name)
);
