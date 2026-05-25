package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.movie.request.CreateRatingRequest;
import com.example.movies.dtos.movie.request.UpdateRatingRequest;
import com.example.movies.dtos.movie.response.RatingSummaryResponse;
import com.example.movies.dtos.movie.response.UserMovieRatingResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface RatingService {

    RatingSummaryResponse createRating(UUID movieId, UUID userId, CreateRatingRequest dto) throws ResourceNotFoundException, ConflictException;

    RatingSummaryResponse updateRating(UUID ratingId, UUID userId, UpdateRatingRequest dto) throws ResourceNotFoundException, ConflictException;

    RatingSummaryResponse findRatingsByMovie(UUID movieId) throws ResourceNotFoundException;

    List<UserMovieRatingResponse> findRatingsByUser(UUID userId);
}
