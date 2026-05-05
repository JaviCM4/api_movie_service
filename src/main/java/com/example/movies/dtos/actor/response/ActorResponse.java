package com.example.movies.dtos.actor.response;

import com.example.movies.models.actor.Actor;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
public class ActorResponse {

    private String name;
    private String urlImage;

    public static ActorResponse from (Actor actor) {
        return new ActorResponse(actor.getName(), actor.getUrlImage());
    }
}
