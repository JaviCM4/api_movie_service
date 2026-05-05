package com.example.movies.services.people;

import com.example.movies.dtos.people.request.UpdatePeopleRequest;
import com.example.movies.dtos.people.response.PeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface PeopleService {

    PeopleResponse updatePeople(UUID id, UpdatePeopleRequest dto) throws ResourceNotFoundException, ConflictException;

    List<PeopleResponse> findAll();
}
