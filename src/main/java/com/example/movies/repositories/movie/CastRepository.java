package com.example.movies.repositories.movie;

import com.example.movies.models.movie.Cast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CastRepository extends JpaRepository<Cast, UUID> {

    List<Cast> findByMovie_Id(UUID movieId);
}
