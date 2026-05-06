package com.example.movies.services.movie;

import com.example.movies.dtos.movie.response.MovieCountryInfoResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.classification.Classification;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieCountryInfo;
import com.example.movies.repositories.classification.ClassificationRepository;
import com.example.movies.repositories.movie.MovieCountryInfoRepository;
import com.example.movies.repositories.movie.MovieRepository;
import com.example.movies.services.movie.inteface.MovieCountryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MovieCountryInfoServiceImplementation implements MovieCountryInfoService {

    private final MovieRepository movieRepository;
    private final ClassificationRepository classificationRepository;
    private final MovieCountryInfoRepository movieCountryInfoRepository;

    @Autowired
    public MovieCountryInfoServiceImplementation(MovieRepository movieRepository, ClassificationRepository classificationRepository, MovieCountryInfoRepository movieCountryInfoRepository) {
        this.movieRepository = movieRepository;
        this.classificationRepository = classificationRepository;
        this.movieCountryInfoRepository = movieCountryInfoRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MovieCountryInfoResponse> addClassification(UUID movieId, UUID classificationId)
            throws ResourceNotFoundException, ConflictException {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        Classification classification = classificationRepository.findWithCountryById(classificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Classification not found with id: " + classificationId));

        if (movieCountryInfoRepository.existsByMovie_IdAndClassification_Id(movieId, classificationId)) {
            throw new ConflictException("Classification '" + classification.getName()
                    + "' is already assigned to this movie");
        }

        UUID countryId = classification.getCountry().getId();
        if (movieCountryInfoRepository.existsByMovie_IdAndCountryId(movieId, countryId)) {
            throw new ConflictException("This movie already has a classification for country '"
                    + classification.getCountry().getName() + "'");
        }

        MovieCountryInfo mci = new MovieCountryInfo();
        mci.setMovie(movie);
        mci.setClassification(classification);
        movieCountryInfoRepository.save(mci);

        return getInfoList(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovieCountryInfoResponse toggleActive(UUID movieCountryInfoId)
            throws ResourceNotFoundException {

        MovieCountryInfo mci = movieCountryInfoRepository.findById(movieCountryInfoId)
                .orElseThrow(() -> new ResourceNotFoundException("MovieCountryInfo not found with id: " + movieCountryInfoId));

        mci.setActive(!mci.isActive());
        return MovieCountryInfoResponse.from(movieCountryInfoRepository.save(mci));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MovieCountryInfoResponse> removeClassification(UUID movieId, UUID movieCountryInfoId)
            throws ResourceNotFoundException {

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }

        MovieCountryInfo mci = movieCountryInfoRepository.findById(movieCountryInfoId)
                .orElseThrow(() -> new ResourceNotFoundException("MovieCountryInfo not found with id: " + movieCountryInfoId));

        movieCountryInfoRepository.delete(mci);
        return getInfoList(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieCountryInfoResponse> getCountryInfo(UUID movieId) throws ResourceNotFoundException {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return getInfoList(movieId);
    }

    private List<MovieCountryInfoResponse> getInfoList(UUID movieId) {
        return movieCountryInfoRepository.findByMovie_Id(movieId)
                .stream()
                .map(MovieCountryInfoResponse::from)
                .toList();
    }
}
