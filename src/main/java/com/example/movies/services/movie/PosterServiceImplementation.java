package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.CreatePosterRequest;
import com.example.movies.dtos.movie.request.UpdatePosterRequest;
import com.example.movies.dtos.movie.response.PosterResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.Poster;
import com.example.movies.repositories.movie.MovieRepository;
import com.example.movies.repositories.movie.PosterRepository;
import com.example.movies.services.movie.inteface.PosterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PosterServiceImplementation implements PosterService {

    private final PosterRepository posterRepository;
    private final MovieRepository movieRepository;

    public PosterServiceImplementation(
            PosterRepository posterRepository,
            MovieRepository movieRepository) {
        this.posterRepository = posterRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PosterResponse> addPoster(UUID movieId, CreatePosterRequest dto)
            throws ResourceNotFoundException, ConflictException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        if (dto.isMain()) {
            posterRepository.findByMovie_IdAndIsMain(movieId, true)
                    .ifPresent(current -> {
                        current.setMain(false);
                        posterRepository.save(current);
                    });
        }

        posterRepository.save(dto.createEntity(movie));
        return postersOf(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PosterResponse> setMainPoster(UUID movieId, UpdatePosterRequest dto)
            throws ResourceNotFoundException {
        // Verificar que la película exista
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }

        Poster newMain = posterRepository.findById(dto.getNewMainPosterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poster not found with id: " + dto.getNewMainPosterId()));

        posterRepository.findByMovie_IdAndIsMain(movieId, true)
                .ifPresent(current -> {
                    if (!current.getId().equals(newMain.getId())) {
                        current.setMain(false);
                        posterRepository.save(current);
                    }
                });

        newMain.setMain(true);
        posterRepository.save(newMain);

        return postersOf(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PosterResponse> deletePoster(UUID posterId)
            throws ResourceNotFoundException, ConflictException {
        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new ResourceNotFoundException("Poster not found with id: " + posterId));

        UUID movieId = poster.getMovie().getId();
        long total = posterRepository.countByMovie_Id(movieId);

        if (total <= 1) {
            throw new ConflictException("Cannot delete the only poster of a movie");
        }

        boolean wasMain = poster.isMain();
        posterRepository.deleteById(posterId);

        if (wasMain) {
            List<Poster> remaining = posterRepository.findByMovie_Id(movieId);
            Poster next = remaining.get(0);
            next.setMain(true);
            posterRepository.save(next);
        }

        return postersOf(movieId);
    }

    private List<PosterResponse> postersOf(UUID movieId) {
        return posterRepository.findByMovie_Id(movieId)
                .stream()
                .map(PosterResponse::from)
                .toList();
    }
}
