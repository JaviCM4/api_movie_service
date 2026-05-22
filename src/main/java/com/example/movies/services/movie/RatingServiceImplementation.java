package com.example.movies.services.movie;

import com.example.movies.client.users.UserClient;
import com.example.movies.dtos.movie.request.CreateRatingRequest;
import com.example.movies.dtos.movie.request.UpdateRatingRequest;
import com.example.movies.dtos.movie.response.RatingResponse;
import com.example.movies.dtos.movie.response.RatingSummaryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieRating;
import com.example.movies.repositories.movie.MovieRatingRepository;
import com.example.movies.repositories.movie.MovieRepository;
import com.example.movies.services.movie.inteface.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RatingServiceImplementation implements RatingService {

    private final MovieRatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final UserClient userClient;

    @Autowired
    public RatingServiceImplementation(MovieRatingRepository ratingRepository, MovieRepository movieRepository,
                                       UserClient userClient) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
        this.userClient = userClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RatingSummaryResponse createRating(UUID movieId, UUID userId, CreateRatingRequest dto)
            throws ResourceNotFoundException, ConflictException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        if (!movie.isAllowRatings()) {
            throw new ConflictException("Ratings are not allowed for this movie");
        }

        if (ratingRepository.findByMovie_IdAndUserId(movieId, userId).isPresent()) {
            throw new ConflictException("User already rated this movie");
        }

        MovieRating rating = dto.createEntity(userId);
        rating.setMovie(movie);
        ratingRepository.save(rating);

        return buildSummary(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RatingSummaryResponse updateRating(UUID ratingId, UUID userId, UpdateRatingRequest dto)
            throws ResourceNotFoundException, ConflictException {
        MovieRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        if (!rating.getUserId().equals(userId)) {
            throw new ConflictException("You don't have permission to update this rating because it was created by another user");
        }

        rating.setScore(dto.getScore());
        ratingRepository.save(rating);

        return buildSummary(rating.getMovie().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryResponse findRatingsByMovie(UUID movieId)
            throws ResourceNotFoundException {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return buildSummary(movieId);
    }

    private RatingSummaryResponse buildSummary(UUID movieId) {
        List<RatingResponse> ratings = ratingRepository.findByMovie_Id(movieId)
                .stream()
                .map(rating -> RatingResponse.from(rating, userClient.getUserName(rating.getUserId())))
                .toList();
        Double average = ratingRepository.findAverageScoreByMovie_Id(movieId);
        return new RatingSummaryResponse(ratings, average);
    }
}
