package com.example.movies.dtos.movie.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateCommentRequest {

    @NotBlank
    @Size(min = 1, max = 1000)
    String content;
}
