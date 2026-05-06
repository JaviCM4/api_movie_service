package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.Cast;
import lombok.Value;

import java.util.UUID;

@Value
public class CastResponse {

    UUID castId;
    UUID actorId;
    String actorName;
    String actorUrlImage;
    String characterName;

    public static CastResponse from(Cast cast) {
        return new CastResponse(
                cast.getId(),
                cast.getActor().getId(),
                cast.getActor().getName(),
                cast.getActor().getUrlImage(),
                cast.getCharacterName()
        );
    }
}
