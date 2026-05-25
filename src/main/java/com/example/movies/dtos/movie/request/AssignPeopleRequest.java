package com.example.movies.dtos.movie.request;

import com.example.movies.models.enums.RolMovieEnum;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MoviePeople;
import com.example.movies.models.people.People;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class AssignPeopleRequest {

    @NotNull(message = "El id de la persona es obligatorio")
    UUID peopleId;

    @NotNull(message = "El rol es obligatorio")
    RolMovieEnum rol;

    public MoviePeople createEntity(Movie movie, People people) {
        MoviePeople moviePeople = new MoviePeople();
        moviePeople.setMovie(movie);
        moviePeople.setPeople(people);
        moviePeople.setRol(rol);
        return moviePeople;
    }
}
