package com.example.movies.repositories.movie;

import com.example.movies.models.movie.MoviePeople;
import com.example.movies.models.enums.RolMovieEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MoviePeopleRepository extends JpaRepository<MoviePeople, UUID> {

    List<MoviePeople> findByMovie_Id(UUID movieId);
}
