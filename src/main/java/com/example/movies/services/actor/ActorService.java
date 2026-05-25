package com.example.movies.services.actor;

import com.example.movies.dtos.actor.response.ActorResponse;
import com.example.movies.dtos.actor.request.CreateActorRequest;
import com.example.movies.dtos.actor.request.UpdateActorRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ActorService {

    ActorResponse createActor(CreateActorRequest dto) throws ConflictException;

    ActorResponse updateActor(UUID actorId, UpdateActorRequest dto) throws ConflictException, ResourceNotFoundException;

    List<ActorResponse> findAllActor();

    ActorResponse toggleActor(UUID actorId) throws ResourceNotFoundException;
}
