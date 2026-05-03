CREATE INDEX idx_movie_active  ON movie(is_active);
CREATE INDEX idx_movie_title   ON movie(title);

CREATE INDEX idx_cast_movie    ON cast_movie(movie_id);
CREATE INDEX idx_cast_actor    ON cast_movie(actor_id);

CREATE INDEX idx_comment_movie ON movie_comment(movie_id);
CREATE INDEX idx_comment_date  ON movie_comment(created_at);

CREATE INDEX idx_rating_movie  ON movie_rating(movie_id);
