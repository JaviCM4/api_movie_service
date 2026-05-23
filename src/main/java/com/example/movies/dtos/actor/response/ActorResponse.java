package com.example.movies.dtos.actor.response;

import com.example.movies.models.actor.Actor;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
public class ActorResponse {

    private UUID id;
    private String name;
    private String urlImage;
    private boolean active;

    public static ActorResponse from(Actor actor) {
        return new ActorResponse(actor.getId(), actor.getName(), actor.getUrlImage(), actor.isActive());
    }
}
