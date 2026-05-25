package com.example.movies.services.movie;

import com.example.movies.dtos.movie.response.MovieCountryInfoResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.classification.Classification;
import com.example.movies.models.country.Country;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieCountryInfo;
import com.example.movies.repositories.classification.ClassificationRepository;
import com.example.movies.repositories.movie.MovieCountryInfoRepository;
import com.example.movies.repositories.movie.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieCountryInfoServiceImplTest {

    private static final UUID MOVIE_ID              = UUID.randomUUID();
    private static final UUID CLASSIFICATION_ID     = UUID.randomUUID();
    private static final UUID MOVIE_COUNTRY_INFO_ID = UUID.randomUUID();
    private static final UUID COUNTRY_ID            = UUID.randomUUID();

    @Mock private MovieRepository              movieRepository;
    @Mock private ClassificationRepository     classificationRepository;
    @Mock private MovieCountryInfoRepository   movieCountryInfoRepository;

    @InjectMocks
    private MovieCountryInfoServiceImplementation movieCountryInfoService;

    // ── addClassification ─────────────────────────────────────────────────

    @Test
    void testAddClassification() throws Exception {
        // Arrange
        ArgumentCaptor<MovieCountryInfo> captor = ArgumentCaptor.forClass(MovieCountryInfo.class);
        Movie          movie          = buildMovie();
        Classification classification = buildClassification();
        MovieCountryInfo saved        = buildMci(movie, classification, true);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(classificationRepository.findWithCountryById(CLASSIFICATION_ID)).thenReturn(Optional.of(classification));
        when(movieCountryInfoRepository.existsByMovie_IdAndClassification_Id(MOVIE_ID, CLASSIFICATION_ID)).thenReturn(false);
        when(movieCountryInfoRepository.existsByMovie_IdAndCountryId(MOVIE_ID, COUNTRY_ID)).thenReturn(false);
        when(movieCountryInfoRepository.save(any(MovieCountryInfo.class))).thenReturn(saved);
        when(movieCountryInfoRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(saved));

        // Act
        List<MovieCountryInfoResponse> result = movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID);

        // Assert
        assertAll(
                () -> verify(movieCountryInfoRepository).save(captor.capture()),
                () -> assertEquals(movie,          captor.getValue().getMovie()),
                () -> assertEquals(classification,  captor.getValue().getClassification()),
                () -> assertTrue(captor.getValue().isActive()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("PG-13",         result.get(0).getClassificationName()),
                () -> assertEquals("United States", result.get(0).getCountryName()),
                () -> assertTrue(result.get(0).isActive())
        );
    }

    @Test
    void testAddClassificationWhenMovieNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID));
        verify(movieCountryInfoRepository, never()).save(any());
    }

    @Test
    void testAddClassificationWhenClassificationNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(classificationRepository.findWithCountryById(CLASSIFICATION_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID));
        verify(movieCountryInfoRepository, never()).save(any());
    }

    @Test
    void testAddClassificationWhenAlreadyAssigned() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(classificationRepository.findWithCountryById(CLASSIFICATION_ID)).thenReturn(Optional.of(buildClassification()));
        when(movieCountryInfoRepository.existsByMovie_IdAndClassification_Id(MOVIE_ID, CLASSIFICATION_ID)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID));
        verify(movieCountryInfoRepository, never()).save(any());
    }

    @Test
    void testAddClassificationWhenCountryAlreadyHasClassification() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(classificationRepository.findWithCountryById(CLASSIFICATION_ID)).thenReturn(Optional.of(buildClassification()));
        when(movieCountryInfoRepository.existsByMovie_IdAndClassification_Id(MOVIE_ID, CLASSIFICATION_ID)).thenReturn(false);
        when(movieCountryInfoRepository.existsByMovie_IdAndCountryId(MOVIE_ID, COUNTRY_ID)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID));
        verify(movieCountryInfoRepository, never()).save(any());
    }

    // ── toggleActive ──────────────────────────────────────────────────────

    @Test
    void testToggleActiveFromTrueToFalse() throws Exception {
        // Arrange
        ArgumentCaptor<MovieCountryInfo> captor = ArgumentCaptor.forClass(MovieCountryInfo.class);
        MovieCountryInfo active   = buildMci(buildMovie(), buildClassification(), true);
        MovieCountryInfo inactive = buildMci(buildMovie(), buildClassification(), false);

        when(movieCountryInfoRepository.findById(MOVIE_COUNTRY_INFO_ID)).thenReturn(Optional.of(active));
        when(movieCountryInfoRepository.save(any(MovieCountryInfo.class))).thenReturn(inactive);

        // Act
        MovieCountryInfoResponse result = movieCountryInfoService.toggleActive(MOVIE_COUNTRY_INFO_ID);

        // Assert
        assertAll(
                () -> verify(movieCountryInfoRepository).save(captor.capture()),
                () -> assertFalse(captor.getValue().isActive()),
                () -> assertFalse(result.isActive())
        );
    }

    @Test
    void testToggleActiveFromFalseToTrue() throws Exception {
        // Arrange
        ArgumentCaptor<MovieCountryInfo> captor = ArgumentCaptor.forClass(MovieCountryInfo.class);
        MovieCountryInfo inactive = buildMci(buildMovie(), buildClassification(), false);
        MovieCountryInfo active   = buildMci(buildMovie(), buildClassification(), true);

        when(movieCountryInfoRepository.findById(MOVIE_COUNTRY_INFO_ID)).thenReturn(Optional.of(inactive));
        when(movieCountryInfoRepository.save(any(MovieCountryInfo.class))).thenReturn(active);

        // Act
        MovieCountryInfoResponse result = movieCountryInfoService.toggleActive(MOVIE_COUNTRY_INFO_ID);

        // Assert
        assertAll(
                () -> verify(movieCountryInfoRepository).save(captor.capture()),
                () -> assertTrue(captor.getValue().isActive()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testToggleActiveWhenNotFound() {
        // Arrange
        when(movieCountryInfoRepository.findById(MOVIE_COUNTRY_INFO_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCountryInfoService.toggleActive(MOVIE_COUNTRY_INFO_ID));
        verify(movieCountryInfoRepository, never()).save(any());
    }

    // ── removeClassification ──────────────────────────────────────────────

    @Test
    void testRemoveClassification() throws Exception {
        // Arrange
        Movie          movie = buildMovie();
        Classification classif = buildClassification();
        MovieCountryInfo mci  = buildMci(movie, classif, true);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(movieCountryInfoRepository.findById(MOVIE_COUNTRY_INFO_ID)).thenReturn(Optional.of(mci));
        when(movieCountryInfoRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of());

        // Act
        List<MovieCountryInfoResponse> result = movieCountryInfoService.removeClassification(MOVIE_ID, MOVIE_COUNTRY_INFO_ID);

        // Assert
        assertAll(
                () -> verify(movieCountryInfoRepository).delete(mci),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void testRemoveClassificationWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCountryInfoService.removeClassification(MOVIE_ID, MOVIE_COUNTRY_INFO_ID));
        verify(movieCountryInfoRepository, never()).delete(any());
    }

    @Test
    void testRemoveClassificationWhenMciNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(movieCountryInfoRepository.findById(MOVIE_COUNTRY_INFO_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCountryInfoService.removeClassification(MOVIE_ID, MOVIE_COUNTRY_INFO_ID));
        verify(movieCountryInfoRepository, never()).delete(any());
    }

    // ── getCountryInfo ────────────────────────────────────────────────────

    @Test
    void testGetCountryInfo() throws Exception {
        // Arrange
        Movie          movie  = buildMovie();
        Classification classif = buildClassification();
        MovieCountryInfo mci  = buildMci(movie, classif, true);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(movieCountryInfoRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(mci));

        // Act
        List<MovieCountryInfoResponse> result = movieCountryInfoService.getCountryInfo(MOVIE_ID);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("PG-13",         result.get(0).getClassificationName()),
                () -> assertEquals(13,              result.get(0).getAgeLimit()),
                () -> assertEquals("United States", result.get(0).getCountryName()),
                () -> assertEquals(COUNTRY_ID,      result.get(0).getCountryId()),
                () -> assertTrue(result.get(0).isActive())
        );
    }

    @Test
    void testGetCountryInfoWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCountryInfoService.getCountryInfo(MOVIE_ID));
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Movie buildMovie() {
        Movie movie = new Movie();
        movie.setId(MOVIE_ID);
        movie.setTitle("Inception");
        return movie;
    }

    private Classification buildClassification() {
        Country country = new Country();
        country.setId(COUNTRY_ID);
        country.setName("United States");

        Classification c = new Classification();
        c.setId(CLASSIFICATION_ID);
        c.setName("PG-13");
        c.setAgeLimit(13);
        c.setCountry(country);
        return c;
    }

    private MovieCountryInfo buildMci(Movie movie, Classification classification, boolean active) {
        MovieCountryInfo mci = new MovieCountryInfo();
        mci.setId(MOVIE_COUNTRY_INFO_ID);
        mci.setMovie(movie);
        mci.setClassification(classification);
        mci.setActive(active);
        return mci;
    }
}
