package com.example.movies.services.actor;

import com.example.movies.dtos.actor.response.ActorResponse;
import com.example.movies.dtos.actor.request.CreateActorRequest;
import com.example.movies.dtos.actor.request.UpdateActorRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.actor.Actor;
import com.example.movies.repositories.actor.ActorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ActorServiceImplementation implements ActorService {

    private final ActorRepository actorRepository;

    @Autowired
    public ActorServiceImplementation(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActorResponse createActor(CreateActorRequest dto) throws ConflictException {
        if (actorRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Ya existe un actor con el nombre " + dto.getName());
        }
        return ActorResponse.from(actorRepository.save(dto.createEntity()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActorResponse updateActor(UUID actorId, UpdateActorRequest dto) throws ConflictException, ResourceNotFoundException {
        Actor actorToUpdate = actorRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor no encontrado con id: " + actorId));

        if (actorRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), actorId)) {
            throw new ConflictException("Ya existe un actor con el nombre " + dto.getName());
        }

        actorToUpdate.setName(dto.getName());
        actorToUpdate.setUrlImage(dto.getUrlImage());
        return ActorResponse.from(actorRepository.save(actorToUpdate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActorResponse> findAllActor() {
        return actorRepository.findAll()
                .stream()
                .map(ActorResponse::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActorResponse toggleActor(UUID actorId) throws ResourceNotFoundException {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor no encontrado con id: " + actorId));
        actor.setActive(!actor.isActive());
        return ActorResponse.from(actorRepository.save(actor));
    }
}
