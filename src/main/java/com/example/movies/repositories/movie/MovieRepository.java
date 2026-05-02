package com.example.movies.repositories.movie;

import com.example.movies.models.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByIsActive(boolean isActive);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByClassification_Id(UUID classificationId);

    @Query("""
        SELECT DISTINCT m
        FROM Movie m
        JOIN MovieCategory mc ON mc.movie.id = m.id
        WHERE mc.category.id = :categoryId
    """)
    List<Movie> findByCategory_Id(UUID categoryId);
}
