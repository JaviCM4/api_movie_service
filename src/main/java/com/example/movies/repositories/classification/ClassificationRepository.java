package com.example.movies.repositories.classification;

import com.example.movies.models.classification.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassificationRepository extends JpaRepository<Classification, UUID> {

    /**
     * Fetches classifications by ID with their associated country eagerly loaded.
     * Used when we need to validate one-classification-per-country constraints.
     */
    @Query("SELECT c FROM Classification c JOIN FETCH c.country WHERE c.id IN :ids")
    List<Classification> findWithCountryByIdIn(@Param("ids") List<UUID> ids);
}
