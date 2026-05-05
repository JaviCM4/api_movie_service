package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.CreateRatingRequest;
import com.example.movies.dtos.movie.request.UpdateRatingRequest;
import com.example.movies.dtos.movie.response.RatingSummaryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieRating;
import com.example.movies.repositories.movie.MovieRatingRepository;
import com.example.movies.repositories.movie.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceImplTest {

    private static final UUID MOVIE_ID  = UUID.randomUUID();
    private static final UUID RATING_ID = UUID.randomUUID();
    private static final UUID USER_ID   = UUID.randomUUID();

    @Mock private MovieRatingRepository ratingRepository;
    @Mock private MovieRepository       movieRepository;

    @InjectMocks
    private RatingServiceImplementation ratingService;

    // ── createRating ──────────────────────────────────────────────────────

    @Test
    void testCreateRating() throws Exception {
        // Arrange
        ArgumentCaptor<MovieRating> captor = ArgumentCaptor.forClass(MovieRating.class);
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 4);

        Movie movie = buildMovie();
        MovieRating saved = buildRating(movie, (short) 4, null);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(ratingRepository.findByMovie_IdAndUserId(MOVIE_ID, USER_ID)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(MovieRating.class))).thenReturn(saved);
        when(ratingRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(saved));
        when(ratingRepository.findAverageScoreByMovie_Id(MOVIE_ID)).thenReturn(4.0);

        // Act
        RatingSummaryResponse result = ratingService.createRating(MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(ratingRepository).save(captor.capture()),
                () -> assertEquals(USER_ID, captor.getValue().getUserId()),
                () -> assertEquals((short) 4, captor.getValue().getScore()),
                () -> assertEquals(1, result.getRatings().size()),
                () -> assertEquals((short) 4, result.getRatings().get(0).getScore()),
                () -> assertFalse(result.getRatings().get(0).isEdited()),
                () -> assertEquals(4.0, result.getAverageScore())
        );
    }

    @Test
    void testCreateRatingWhenMovieNotFound() {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 3);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ratingService.createRating(MOVIE_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void testCreateRatingWhenRatingsNotAllowed() {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 3);

        Movie movie = buildMovie();
        movie.setAllowRatings(false);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

        // Assert
        assertThrows(ConflictException.class,
                () -> ratingService.createRating(MOVIE_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void testCreateRatingWhenAlreadyRated() {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 3);

        Movie movie = buildMovie();
        MovieRating existing = buildRating(movie, (short) 5, null);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(ratingRepository.findByMovie_IdAndUserId(MOVIE_ID, USER_ID)).thenReturn(Optional.of(existing));

        // Assert
        assertThrows(ConflictException.class,
                () -> ratingService.createRating(MOVIE_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    // ── updateRating ──────────────────────────────────────────────────────

    @Test
    void testUpdateRating() throws Exception {
        // Arrange
        ArgumentCaptor<MovieRating> captor = ArgumentCaptor.forClass(MovieRating.class);
        UpdateRatingRequest request = new UpdateRatingRequest((short) 2);

        Movie movie = buildMovie();
        MovieRating existing = buildRating(movie, (short) 5, null);
        MovieRating updated  = buildRating(movie, (short) 2, LocalDateTime.now());

        when(ratingRepository.findById(RATING_ID)).thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(MovieRating.class))).thenReturn(updated);
        when(ratingRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(updated));
        when(ratingRepository.findAverageScoreByMovie_Id(MOVIE_ID)).thenReturn(2.0);

        // Act
        RatingSummaryResponse result = ratingService.updateRating(RATING_ID, request);

        // Assert
        assertAll(
                () -> verify(ratingRepository).save(captor.capture()),
                () -> assertEquals((short) 2, captor.getValue().getScore()),
                () -> assertEquals(1, result.getRatings().size()),
                () -> assertTrue(result.getRatings().get(0).isEdited()),
                () -> assertEquals(2.0, result.getAverageScore())
        );
    }

    @Test
    void testUpdateRatingWhenRatingNotFound() {
        // Arrange
        UpdateRatingRequest request = new UpdateRatingRequest((short) 3);
        when(ratingRepository.findById(RATING_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ratingService.updateRating(RATING_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    // ── findRatingsByMovie ────────────────────────────────────────────────

    @Test
    void testFindRatingsByMovie() throws Exception {
        // Arrange
        Movie movie = buildMovie();
        MovieRating r1 = buildRating(movie, (short) 5, null);
        MovieRating r2 = buildRating(movie, (short) 3, LocalDateTime.now());
        r2.setId(UUID.randomUUID());

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(ratingRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(r1, r2));
        when(ratingRepository.findAverageScoreByMovie_Id(MOVIE_ID)).thenReturn(4.0);

        // Act
        RatingSummaryResponse result = ratingService.findRatingsByMovie(MOVIE_ID);

        // Assert
        assertAll(
                () -> assertEquals(2, result.getRatings().size()),
                () -> assertEquals((short) 5, result.getRatings().get(0).getScore()),
                () -> assertFalse(result.getRatings().get(0).isEdited()),
                () -> assertEquals((short) 3, result.getRatings().get(1).getScore()),
                () -> assertTrue(result.getRatings().get(1).isEdited()),
                () -> assertEquals(4.0, result.getAverageScore())
        );
    }

    @Test
    void testFindRatingsByMovieWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ratingService.findRatingsByMovie(MOVIE_ID));
        verify(ratingRepository, never()).findByMovie_Id(any());
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private Movie buildMovie() {
        Movie m = new Movie();
        m.setId(MOVIE_ID);
        m.setTitle("Inception");
        m.setAllowComments(true);
        m.setAllowRatings(true);
        return m;
    }

    private MovieRating buildRating(Movie movie, short score, LocalDateTime updatedAt) {
        MovieRating r = new MovieRating();
        r.setId(RATING_ID);
        r.setMovie(movie);
        r.setUserId(USER_ID);
        r.setScore(score);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(updatedAt);
        return r;
    }
}
