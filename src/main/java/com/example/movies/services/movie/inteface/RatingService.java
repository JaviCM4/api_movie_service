package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.movie.request.CreateRatingRequest;
import com.example.movies.dtos.movie.request.UpdateRatingRequest;
import com.example.movies.dtos.movie.response.RatingSummaryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.UUID;

public interface RatingService {

    RatingSummaryResponse createRating(UUID movieId, CreateRatingRequest dto) throws ResourceNotFoundException, ConflictException;

    RatingSummaryResponse updateRating(UUID ratingId, UpdateRatingRequest dto) throws ResourceNotFoundException, ConflictException;

    RatingSummaryResponse findRatingsByMovie(UUID movieId) throws ResourceNotFoundException;
}
