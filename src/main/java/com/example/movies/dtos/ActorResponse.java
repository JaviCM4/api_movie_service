package com.example.movies.dtos;

import com.example.movies.models.actor.Actor;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ActorResponse {

    private String name;
    private String urlImage;

    public ActorResponse(Actor actor) {
        this.name = actor.getName();
        this.urlImage = actor.getUrlImage();
    }
}
