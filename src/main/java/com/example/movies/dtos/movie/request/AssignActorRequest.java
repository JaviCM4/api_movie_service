package com.example.movies.dtos.movie.request;

import com.example.movies.models.actor.Actor;
import com.example.movies.models.movie.Cast;
import com.example.movies.models.movie.Movie;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class AssignActorRequest {

    @NotNull(message = "El id del actor es obligatorio")
    UUID actorId;

    @Size(max = 255, message = "El nombre del personaje no puede superar los 255 caracteres")
    String characterName;

    public Cast createEntity(Movie movie, Actor actor) {
        Cast cast = new Cast();
        cast.setMovie(movie);
        cast.setActor(actor);
        cast.setCharacterName(characterName);
        return cast;
    }
}
