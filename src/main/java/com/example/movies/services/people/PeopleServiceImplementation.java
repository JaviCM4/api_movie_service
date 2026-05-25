package com.example.movies.services.people;

import com.example.movies.dtos.people.request.CreatePeopleRequest;
import com.example.movies.dtos.people.request.UpdatePeopleRequest;
import com.example.movies.dtos.people.response.PeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.people.People;
import com.example.movies.repositories.people.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PeopleServiceImplementation implements PeopleService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PeopleServiceImplementation(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PeopleResponse createPeople(CreatePeopleRequest dto) throws ConflictException {
        if (peopleRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Ya existe una persona con el nombre '" + dto.getName() + "'");
        }
        return PeopleResponse.from(peopleRepository.save(dto.createEntity()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PeopleResponse updatePeople(UUID id, UpdatePeopleRequest dto)
            throws ResourceNotFoundException, ConflictException {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + id));

        if (peopleRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new ConflictException("Ya existe una persona con el nombre '" + dto.getName() + "'");
        }

        people.setName(dto.getName());
        return PeopleResponse.from(peopleRepository.save(people));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeopleResponse> findAll() {
        return peopleRepository.findAll()
                .stream()
                .map(PeopleResponse::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PeopleResponse togglePeople(UUID id) throws ResourceNotFoundException {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + id));
        people.setActive(!people.isActive());
        return PeopleResponse.from(peopleRepository.save(people));
    }
}
