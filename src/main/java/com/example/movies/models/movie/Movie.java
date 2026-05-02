package com.example.movies.models.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.movies.models.classification.Classification;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movie")
@Data
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classification_id", nullable = false)
    @JsonIgnore
    private Classification classification;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "synopsis", nullable = false, columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "trailer_link", length = 500)
    private String trailerLink;

    @Column(name = "original_language", length = 50)
    private String originalLanguage;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
