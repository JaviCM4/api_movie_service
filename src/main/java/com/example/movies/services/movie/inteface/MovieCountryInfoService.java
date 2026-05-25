package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.movie.response.MovieCountryInfoResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface MovieCountryInfoService {

    List<MovieCountryInfoResponse> addClassification(UUID movieId, UUID classificationId)
            throws ResourceNotFoundException, ConflictException;

    MovieCountryInfoResponse toggleActive(UUID movieCountryInfoId)
            throws ResourceNotFoundException;

    List<MovieCountryInfoResponse> removeClassification(UUID movieId, UUID movieCountryInfoId)
            throws ResourceNotFoundException;

    List<MovieCountryInfoResponse> getCountryInfo(UUID movieId)
            throws ResourceNotFoundException;
}
