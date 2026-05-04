package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.Poster;
import lombok.Value;

@Value
public class PosterResponse {

    String urlImage;
    boolean isMain;

    public static PosterResponse from(Poster poster) {
        return new PosterResponse(poster.getUrlImage(), poster.isMain());
    }
}
