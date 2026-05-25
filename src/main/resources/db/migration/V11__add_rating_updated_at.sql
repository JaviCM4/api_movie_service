-- Add updated_at to movie_rating (NULL means not edited)
ALTER TABLE movie_rating
    ADD COLUMN updated_at TIMESTAMP;
