package com.example.movies.repositories.classification;

import com.example.movies.models.classification.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassificationRepository extends JpaRepository<Classification, UUID> {

}
