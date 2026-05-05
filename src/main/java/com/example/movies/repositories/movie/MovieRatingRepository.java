package com.example.movies.repositories.movie;

import com.example.movies.models.movie.MovieRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRatingRepository extends JpaRepository<MovieRating, UUID> {

    List<MovieRating> findByMovie_Id(UUID movieId);

    Optional<MovieRating> findByMovie_IdAndUserId(UUID movieId, UUID userId);

    @Query("SELECT AVG(r.score) FROM MovieRating r WHERE r.movie.id = :movieId")
    Double findAverageScoreByMovie_Id(UUID movieId);
}
