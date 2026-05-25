package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.movie.request.CreateCastRequest;
import com.example.movies.dtos.movie.request.UpdateCastRequest;
import com.example.movies.dtos.movie.response.CastResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface CastService {

    List<CastResponse> addActor(UUID movieId, CreateCastRequest dto)
            throws ResourceNotFoundException, ConflictException;

    CastResponse updateCharacterName(UUID castId, UpdateCastRequest dto)
            throws ResourceNotFoundException;

    List<CastResponse> removeActor(UUID movieId, UUID castId)
            throws ResourceNotFoundException;

    List<CastResponse> getCast(UUID movieId)
            throws ResourceNotFoundException;
}
