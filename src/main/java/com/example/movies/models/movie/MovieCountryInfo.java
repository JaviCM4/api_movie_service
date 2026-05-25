package com.example.movies.models.movie;

import com.example.movies.models.classification.Classification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents a country-scoped classification assignment for a movie.
 * A movie may have at most one active classification per country.
 * This class lives in ClassificationMovie.java — rename to MovieCountryInfo.java.
 */
@Entity
@Table(
        name = "movie_country_info",
        uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "classification_id"})
)
@Data
@NoArgsConstructor
public class MovieCountryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classification_id", nullable = false)
    @JsonIgnore
    private Classification classification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnore
    private Movie movie;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
