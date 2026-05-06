package com.example.movies.repositories.movie;

import com.example.movies.models.movie.MovieCountryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MovieCountryInfo.
 * This file lives in ClassificationMovieRepository.java — rename to MovieCountryInfoRepository.java.
 */
@Repository
public interface MovieCountryInfoRepository extends JpaRepository<MovieCountryInfo, UUID> {

    List<MovieCountryInfo> findByMovie_Id(UUID movieId);

    boolean existsByMovie_IdAndClassification_Id(UUID movieId, UUID classificationId);

    /**
     * Batch-fetches the active MovieCountryInfo for a specific country.
     * Country is JOIN FETCHed to avoid lazy loading in the DTO mapping.
     */
    @Query("""
        SELECT mci FROM MovieCountryInfo mci
        JOIN FETCH mci.classification c
        JOIN FETCH c.country co
        WHERE mci.movie.id IN :movieIds
          AND mci.isActive = true
          AND co.id = :countryId
    """)
    List<MovieCountryInfo> findActiveByCountryAndMovieIdIn(
            @Param("movieIds") List<UUID> movieIds,
            @Param("countryId") UUID countryId);
}
