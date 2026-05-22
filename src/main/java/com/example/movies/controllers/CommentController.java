package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateCommentRequest;
import com.example.movies.dtos.movie.request.UpdateCommentRequest;
import com.example.movies.dtos.movie.response.CommentResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/v1/movies/{movieId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID movieId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(commentService.findCommentsByMovie(movieId));
    }

    @PostMapping("/v1/movies/{movieId}/comments")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID movieId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) throws ConflictException, ResourceNotFoundException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(movieId, userId, request));
    }

    @PatchMapping("/v1/comments/{commentId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) throws ResourceNotFoundException, ConflictException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(commentService.updateComment(commentId, userId, request));
    }

    @DeleteMapping("/v1/comments/{commentId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal Jwt jwt
    ) throws ResourceNotFoundException, ConflictException {
        UUID userId = UUID.fromString(jwt.getSubject());
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
