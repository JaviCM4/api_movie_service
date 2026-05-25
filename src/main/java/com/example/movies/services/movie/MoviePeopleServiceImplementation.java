package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.AssignPeopleRequest;
import com.example.movies.dtos.movie.response.MoviePeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.enums.RolMovieEnum;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MoviePeople;
import com.example.movies.models.people.People;
import com.example.movies.repositories.movie.MoviePeopleRepository;
import com.example.movies.repositories.movie.MovieRepository;
import com.example.movies.repositories.people.PeopleRepository;
import com.example.movies.services.movie.inteface.MoviePeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MoviePeopleServiceImplementation implements MoviePeopleService {

    private final MovieRepository movieRepository;
    private final PeopleRepository peopleRepository;
    private final MoviePeopleRepository moviePeopleRepository;

    @Autowired
    public MoviePeopleServiceImplementation(MovieRepository movieRepository,
                                            PeopleRepository peopleRepository,
                                            MoviePeopleRepository moviePeopleRepository) {
        this.movieRepository = movieRepository;
        this.peopleRepository = peopleRepository;
        this.moviePeopleRepository = moviePeopleRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MoviePeopleResponse> addPerson(UUID movieId, AssignPeopleRequest dto)
            throws ResourceNotFoundException, ConflictException {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada con id: " + movieId));

        People people = peopleRepository.findById(dto.getPeopleId())
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + dto.getPeopleId()));

        if (moviePeopleRepository.existsByMovie_IdAndPeople_IdAndRol(movieId, dto.getPeopleId(), dto.getRol())) {
            throw new ConflictException("'" + people.getName() + "' ya está asignado/a como "
                    + dto.getRol() + " en esta película");
        }

        MoviePeople moviePeople = dto.createEntity(movie, people);
        moviePeopleRepository.save(moviePeople);
        return getPeopleList(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MoviePeopleResponse> updateRol(UUID movieId, UUID moviePeopleId, RolMovieEnum rol)
            throws ResourceNotFoundException, ConflictException {

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Película no encontrada con id: " + movieId);
        }

        MoviePeople moviePeople = moviePeopleRepository.findById(moviePeopleId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada de persona en película no encontrada con id: " + moviePeopleId));

        if (moviePeopleRepository.existsByMovie_IdAndPeople_IdAndRol(movieId, moviePeople.getPeople().getId(), rol)) {
            throw new ConflictException("'" + moviePeople.getPeople().getName() + "' ya está asignado/a como "
                    + rol + " en esta película");
        }

        moviePeople.setRol(rol);
        moviePeopleRepository.save(moviePeople);
        return getPeopleList(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MoviePeopleResponse> removePerson(UUID movieId, UUID moviePeopleId)
            throws ResourceNotFoundException {

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Película no encontrada con id: " + movieId);
        }

        MoviePeople moviePeople = moviePeopleRepository.findById(moviePeopleId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada de persona en película no encontrada con id: " + moviePeopleId));

        moviePeopleRepository.delete(moviePeople);
        return getPeopleList(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoviePeopleResponse> getPeople(UUID movieId)
            throws ResourceNotFoundException {

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Película no encontrada con id: " + movieId);
        }

        return getPeopleList(movieId);
    }

    private List<MoviePeopleResponse> getPeopleList(UUID movieId) {
        return moviePeopleRepository.findByMovie_Id(movieId)
                .stream()
                .map(MoviePeopleResponse::from)
                .toList();
    }
}
