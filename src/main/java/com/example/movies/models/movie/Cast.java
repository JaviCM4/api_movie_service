package com.example.movies.models.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.movies.models.actor.Actor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cast_movie")
@Data
@NoArgsConstructor
public class Cast {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnore
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    @JsonIgnore
    private Actor actor;

    @Column(name = "character_name", length = 255)
    private String characterName;
}
