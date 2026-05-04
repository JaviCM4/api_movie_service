package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.CreateMovieRequest;
import com.example.movies.dtos.movie.response.MovieDetailResponse;
import com.example.movies.dtos.movie.request.UpdateMovieRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface MovieService {

    void createMovie(CreateMovieRequest dto) throws ResourceNotFoundException, ConflictException;

    void updateMovie(UUID movieId, UpdateMovieRequest dto) throws ResourceNotFoundException;

    List<MovieDetailResponse> findAllMoviesByCountry(UUID countryId);
}
