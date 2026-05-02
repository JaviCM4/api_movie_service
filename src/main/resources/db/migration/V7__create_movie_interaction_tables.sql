CREATE TABLE movie_comment (
    id         UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id   UUID      NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL,
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE movie_rating (
    id         UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id   UUID      NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL,
    score      SMALLINT  NOT NULL CHECK (score BETWEEN 1 AND 5),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (movie_id, user_id)
);
