package com.example.movies.models.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.movies.models.people.People;
import com.example.movies.models.enums.RolMovieEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "movie_people")
@Data
@NoArgsConstructor
public class MoviePeople {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnore
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "people_id", nullable = false)
    @JsonIgnore
    private People people;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolMovieEnum rol;
}
