package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.movie.request.CreateCommentRequest;
import com.example.movies.dtos.movie.request.UpdateCommentRequest;
import com.example.movies.dtos.movie.response.CommentResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentResponse createComment(UUID movieId, CreateCommentRequest dto) throws ResourceNotFoundException, ConflictException;

    CommentResponse updateComment(UUID commentId, UpdateCommentRequest dto) throws ResourceNotFoundException;

    void deleteComment(UUID commentId) throws ResourceNotFoundException;

    List<CommentResponse> findCommentsByMovie(UUID movieId) throws ResourceNotFoundException;
}
