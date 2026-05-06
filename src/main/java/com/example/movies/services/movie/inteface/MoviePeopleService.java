package com.example.movies.services.movie.inteface;

import com.example.movies.dtos.movie.request.AssignPeopleRequest;
import com.example.movies.dtos.movie.response.MoviePeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.enums.RolMovieEnum;

import java.util.List;
import java.util.UUID;

public interface MoviePeopleService {

    List<MoviePeopleResponse> addPerson(UUID movieId, AssignPeopleRequest dto)
            throws ResourceNotFoundException, ConflictException;

    List<MoviePeopleResponse> updateRol(UUID movieId, UUID moviePeopleId, RolMovieEnum rol)
            throws ResourceNotFoundException, ConflictException;

    List<MoviePeopleResponse> removePerson(UUID movieId, UUID moviePeopleId)
            throws ResourceNotFoundException;

    List<MoviePeopleResponse> getPeople(UUID movieId)
            throws ResourceNotFoundException;
}
