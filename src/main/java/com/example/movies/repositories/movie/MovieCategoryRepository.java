package com.example.movies.repositories.movie;

import com.example.movies.models.movie.MovieCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieCategoryRepository extends JpaRepository<MovieCategory, UUID> {

    List<MovieCategory> findByMovie_Id(UUID movieId);
}
