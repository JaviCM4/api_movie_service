package com.example.movies.dtos.movie.request;

import com.example.movies.models.movie.MovieComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateCommentRequest {

    @NotBlank
    @Size(min = 1, max = 1000)
    String content;

    public MovieComment createEntity(UUID userId) {
        MovieComment movieComment = new MovieComment();
        movieComment.setUserId(userId);
        movieComment.setContent(content);
        return movieComment;
    }
}
