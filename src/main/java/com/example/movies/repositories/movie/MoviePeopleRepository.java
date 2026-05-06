package com.example.movies.repositories.movie;

import com.example.movies.models.movie.MoviePeople;
import com.example.movies.models.enums.RolMovieEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MoviePeopleRepository extends JpaRepository<MoviePeople, UUID> {

    List<MoviePeople> findByMovie_Id(UUID movieId);

    boolean existsByMovie_IdAndPeople_Id(UUID movieId, UUID peopleId);

    boolean existsByMovie_IdAndPeople_IdAndRol(UUID movieId, UUID peopleId, RolMovieEnum rol);

    @Query("SELECT mp FROM MoviePeople mp JOIN FETCH mp.people WHERE mp.movie.id IN :movieIds")
    List<MoviePeople> findWithPeopleByMovieIdIn(@Param("movieIds") List<UUID> movieIds);
}
