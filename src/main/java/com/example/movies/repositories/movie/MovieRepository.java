package com.example.movies.repositories.movie;

import com.example.movies.models.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("""
        SELECT DISTINCT m FROM Movie m
        WHERE m.id IN (
            SELECT mci.movie.id FROM MovieCountryInfo mci
            JOIN mci.classification c
            WHERE mci.isActive = true AND c.country.id = :countryId
        )
    """)
    List<Movie> findActiveByCountryId(@Param("countryId") UUID countryId);

    @Query("""
        SELECT DISTINCT m
        FROM Movie m
        JOIN MovieCategory mc ON mc.movie.id = m.id
        WHERE mc.category.id = :categoryId
    """)
    List<Movie> findByCategory_Id(UUID categoryId);

    @Query("""
        SELECT DISTINCT m FROM Movie m
        LEFT JOIN MovieCategory mc ON mc.movie.id = m.id
        WHERE m.id IN (
            SELECT mci.movie.id FROM MovieCountryInfo mci
            WHERE mci.isActive = true AND mci.classification.country.id = :countryId
        )
        AND (CAST(:title AS String) IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', CAST(:title AS String), '%')))
        AND (CAST(:categoryId AS String) IS NULL OR mc.category.id = :categoryId)
        AND (CAST(:classificationId AS String) IS NULL OR m.id IN (
            SELECT mci2.movie.id FROM MovieCountryInfo mci2
            WHERE mci2.isActive = true
              AND mci2.classification.country.id = :countryId
              AND mci2.classification.id = :classificationId
        ))
    """)
    List<Movie> findActiveByCountryIdWithFilters(@Param("countryId") UUID countryId,
                                                 @Param("title") String title,
                                                 @Param("categoryId") UUID categoryId,
                                                 @Param("classificationId") UUID classificationId);
}
