package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.Cast;
import lombok.Value;

@Value
public class CastDetailResponse {

    String actorName;
    String actorImageUrl;
    String characterName;

    public static CastDetailResponse from(Cast cast) {
        return new CastDetailResponse(
                cast.getActor().getName(),
                cast.getActor().getUrlImage(),
                cast.getCharacterName()
        );
    }
}
