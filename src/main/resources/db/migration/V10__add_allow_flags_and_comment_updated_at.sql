-- Add permission flags to movie table
ALTER TABLE movie
    ADD COLUMN allow_comments BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN allow_ratings  BOOLEAN NOT NULL DEFAULT TRUE;

-- Add updated_at to movie_comment (NULL means not edited)
ALTER TABLE movie_comment
    ADD COLUMN updated_at TIMESTAMP;
