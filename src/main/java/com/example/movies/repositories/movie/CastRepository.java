package com.example.movies.repositories.movie;

import com.example.movies.models.movie.Cast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CastRepository extends JpaRepository<Cast, UUID> {

    List<Cast> findByMovie_Id(UUID movieId);

    boolean existsByMovie_IdAndActor_Id(UUID movieId, UUID actorId);

    @Query("SELECT c FROM Cast c JOIN FETCH c.actor WHERE c.movie.id IN :movieIds")
    List<Cast> findWithActorByMovieIdIn(@Param("movieIds") List<UUID> movieIds);
}
