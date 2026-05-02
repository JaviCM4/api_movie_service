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

CREATE TABLE cast (
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
