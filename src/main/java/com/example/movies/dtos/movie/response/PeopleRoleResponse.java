package com.example.movies.dtos.movie.response;

import com.example.movies.models.enums.RolMovieEnum;
import com.example.movies.models.movie.MoviePeople;
import lombok.Value;

@Value
public class PeopleRoleResponse {

    String name;
    RolMovieEnum role;

    public static PeopleRoleResponse from(MoviePeople mp) {
        return new PeopleRoleResponse(mp.getPeople().getName(), mp.getRol());
    }
}
