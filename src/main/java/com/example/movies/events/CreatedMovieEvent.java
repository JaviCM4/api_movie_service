package com.example.movies.events;

import lombok.Value;

import java.util.UUID;


@Value
public class CreatedMovieEvent {
    UUID idMovie;
    String title;

    public static CreatedMovieEvent from(UUID idMovie, String title) {
        return new CreatedMovieEvent(idMovie, title);
    }
}
