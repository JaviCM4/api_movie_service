package com.example.movies.dtos.actor.request;

import com.example.movies.models.actor.Actor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

@Value
public class CreateActorRequest {

    @NotBlank
    @Size(max = 75)
    String name;

    @URL
    @Size(max = 500)
    String urlImage;

    public Actor createEntity() {
        Actor actor = new Actor();
        actor.setName(name.trim());
        actor.setUrlImage(urlImage != null ? urlImage.trim() : null);
        return actor;
    }
}
