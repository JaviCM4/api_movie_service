package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.MovieComment;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class UserMovieCommentResponse {

    UUID id;
    String content;
    LocalDateTime createdAt;
    boolean edited;
    UUID movieId;
    String movieTitle;
    String posterUrl;

    public static UserMovieCommentResponse from(MovieComment comment, String posterUrl) {
        return new UserMovieCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt() != null,
                comment.getMovie().getId(),
                comment.getMovie().getTitle(),
                posterUrl
        );
    }
}
