package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.MovieComment;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class CommentResponse {

    UUID id;
    UUID userId;
    String content;
    LocalDateTime createdAt;
    boolean edited;

    public static CommentResponse from(MovieComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt() != null
        );
    }
}
