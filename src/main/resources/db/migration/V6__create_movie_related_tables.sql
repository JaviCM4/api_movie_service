-- Reemplaza classification_movie: la clasificación varía por país
-- y aquí también vive el is_active por país
CREATE TABLE movie_country_info (
    id                UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    classification_id UUID      NOT NULL REFERENCES classification(id),
    movie_id          UUID      NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    is_active         BOOLEAN   NOT NULL DEFAULT TRUE,
    UNIQUE (movie_id, classification_id)
);

CREATE TABLE poster (
    id         UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id   UUID         NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    url_image  VARCHAR(500) NOT NULL,
    is_main    BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE movie_categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id    UUID NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES category(id),
    UNIQUE (movie_id, category_id)
);

CREATE TABLE cast_movie (
    id             UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id       UUID         NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    actor_id       UUID         NOT NULL REFERENCES actor(id),
    character_name VARCHAR(255)
);

CREATE TABLE movie_people (
    id        UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id  UUID           NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    people_id UUID           NOT NULL REFERENCES people(id),
    rol       rol_movie_enum NOT NULL,
    UNIQUE (movie_id, people_id, rol)
);
