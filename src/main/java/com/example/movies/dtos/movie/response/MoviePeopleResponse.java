package com.example.movies.dtos.movie.response;

import com.example.movies.models.enums.RolMovieEnum;
import com.example.movies.models.movie.MoviePeople;
import lombok.Value;

import java.util.UUID;

@Value
public class MoviePeopleResponse {

    UUID moviePeopleId;
    UUID peopleId;
    String peopleName;
    RolMovieEnum rol;

    public static MoviePeopleResponse from(MoviePeople mp) {
        return new MoviePeopleResponse(
                mp.getId(),
                mp.getPeople().getId(),
                mp.getPeople().getName(),
                mp.getRol()
        );
    }
}
