package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class UpdateCommentRequest {

    @NotNull
    UUID userId;

    @NotBlank
    @Size(min = 1, max = 1000)
    String content;
}
