package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.movie.request.CreatePosterRequest;
import com.example.movies.dtos.movie.request.UpdatePosterRequest;
import com.example.movies.dtos.movie.response.PosterResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface PosterService {

    List<PosterResponse> getPosters(UUID movieId) throws ResourceNotFoundException;

    List<PosterResponse> addPoster(UUID movieId, CreatePosterRequest dto) throws ResourceNotFoundException, ConflictException;

    List<PosterResponse> setMainPoster(UUID movieId, UpdatePosterRequest dto) throws ResourceNotFoundException;

    List<PosterResponse> deletePoster(UUID posterId) throws ResourceNotFoundException, ConflictException;
}
