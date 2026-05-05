package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.Poster;
import lombok.Value;

import java.util.UUID;

@Value
public class PosterResponse {

    UUID id;
    String urlImage;
    boolean isMain;

    public static PosterResponse from(Poster poster) {
        return new PosterResponse(poster.getId(), poster.getUrlImage(), poster.isMain());
    }
}
