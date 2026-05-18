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
    public ResponseEntity<CommentResponse> createComment(@PathVariable UUID movieId, @Valid @RequestBody CreateCommentRequest request)
            throws ConflictException, ResourceNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(movieId, request));
    }

    @PatchMapping("/v1/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable UUID commentId, @Valid @RequestBody UpdateCommentRequest request) throws ResourceNotFoundException, ConflictException {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/v1/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId, @RequestParam UUID userId)
            throws ResourceNotFoundException, ConflictException {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
