package com.example.movies.events;


import lombok.Value;

import java.util.UUID;

@Value
public class UpdatedMovieEvent {
    UUID idMovie;
    String title;

    public static UpdatedMovieEvent from(UUID idMovie, String title) {
        return new UpdatedMovieEvent(idMovie, title);
    }
}
