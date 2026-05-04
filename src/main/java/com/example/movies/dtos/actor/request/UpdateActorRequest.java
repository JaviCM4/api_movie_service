package com.example.movies.dtos.actor.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.validator.constraints.URL;

@Value
public class UpdateActorRequest {

    @NotBlank
    @Size(max = 75)
    String name;

    @URL
    @Size(max = 500)
    String urlImage;
}
