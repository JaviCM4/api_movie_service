package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.CreateCastRequest;
import com.example.movies.dtos.movie.request.UpdateCastRequest;
import com.example.movies.dtos.movie.response.CastResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.actor.Actor;
import com.example.movies.models.movie.Cast;
import com.example.movies.models.movie.Movie;
import com.example.movies.repositories.actor.ActorRepository;
import com.example.movies.repositories.movie.CastRepository;
import com.example.movies.repositories.movie.MovieRepository;
import com.example.movies.services.movie.inteface.CastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CastServiceImplementation implements CastService {

    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final CastRepository castRepository;

    @Autowired
    public CastServiceImplementation(MovieRepository movieRepository, ActorRepository actorRepository, CastRepository castRepository) {
        this.movieRepository = movieRepository;
        this.actorRepository = actorRepository;
        this.castRepository = castRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CastResponse> addActor(UUID movieId, CreateCastRequest dto)
            throws ResourceNotFoundException, ConflictException {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        Actor actor = actorRepository.findById(dto.getActorId())
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + dto.getActorId()));

        if (castRepository.existsByMovie_IdAndActor_Id(movieId, dto.getActorId())) {
            throw new ConflictException("Actor '" + actor.getName() + "' is already in the cast of this movie");
        }

        Cast cast = dto.createEntity(movie, actor);
        castRepository.save(cast);
        return getCastList(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CastResponse updateCharacterName(UUID castId, UpdateCastRequest dto)
            throws ResourceNotFoundException {

        Cast cast = castRepository.findById(castId)
                .orElseThrow(() -> new ResourceNotFoundException("Cast entry not found with id: " + castId));

        cast.setCharacterName(dto.getCharacterName());
        return CastResponse.from(castRepository.save(cast));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CastResponse> removeActor(UUID movieId, UUID castId)
            throws ResourceNotFoundException {

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }

        Cast cast = castRepository.findById(castId)
                .orElseThrow(() -> new ResourceNotFoundException("Cast entry not found with id: " + castId));

        castRepository.delete(cast);
        return getCastList(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CastResponse> getCast(UUID movieId) throws ResourceNotFoundException {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return getCastList(movieId);
    }

    private List<CastResponse> getCastList(UUID movieId) {
        return castRepository.findByMovie_Id(movieId)
                .stream()
                .map(CastResponse::from)
                .toList();
    }
}
