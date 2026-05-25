package com.example.movies.repositories.movie;

import com.example.movies.models.movie.MovieComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieCommentRepository extends JpaRepository<MovieComment, UUID> {

    List<MovieComment> findByMovie_IdOrderByCreatedAtDesc(UUID movieId);

    List<MovieComment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
