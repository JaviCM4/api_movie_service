package com.example.movies.services;

import com.example.movies.dtos.ActorResponse;
import com.example.movies.dtos.CreateActorRequest;
import com.example.movies.dtos.UpdateActorRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ActorService {

    void createActor(CreateActorRequest dto) throws ConflictException;

    void updateActor(UUID actorId, UpdateActorRequest dto) throws ConflictException, ResourceNotFoundException;

    List<ActorResponse> findAllActor();
}
