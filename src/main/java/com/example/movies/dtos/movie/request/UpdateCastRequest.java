package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateCastRequest {

    @NotBlank
    @Size(max = 255)
    String characterName;
}
