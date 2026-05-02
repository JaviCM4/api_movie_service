package com.example.movies.repositories.movie;

import com.example.movies.models.movie.Poster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PosterRepository extends JpaRepository<Poster, UUID> {

    List<Poster> findByMovie_Id(UUID movieId);

    Optional<Poster> findByMovie_IdAndIsMain(UUID movieId, boolean isMain);
}
