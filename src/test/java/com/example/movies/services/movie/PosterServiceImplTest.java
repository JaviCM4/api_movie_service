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
public class PosterServiceImplTest {

    private static final UUID MOVIE_ID    = UUID.randomUUID();
    private static final UUID POSTER_ID_1 = UUID.randomUUID();
    private static final UUID POSTER_ID_2 = UUID.randomUUID();

    @Mock private PosterRepository posterRepository;
    @Mock private MovieRepository  movieRepository;

    @InjectMocks
    private PosterServiceImplementation posterService;

    // ── addPoster ─────────────────────────────────────────────────────────

    @Test
    void testAddPosterNotMain() throws Exception {
        // Arrange
        CreatePosterRequest request = new CreatePosterRequest("https://img.com/p.jpg", false);
        Movie movie = buildMovie();
        Poster saved = buildPoster(POSTER_ID_1, movie, "https://img.com/p.jpg", false);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(posterRepository.save(any(Poster.class))).thenReturn(saved);
        when(posterRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(saved));

        // Act
        List<PosterResponse> result = posterService.addPoster(MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(posterRepository, never()).findByMovie_IdAndIsMain(any(), eq(true)),
                () -> assertEquals(1, result.size()),
                () -> assertFalse(result.get(0).isMain())
        );
    }

    @Test
    void testAddPosterAsMainDemotesCurrentMain() throws Exception {
        // Arrange
        ArgumentCaptor<Poster> captor = ArgumentCaptor.forClass(Poster.class);
        CreatePosterRequest request = new CreatePosterRequest("https://img.com/new.jpg", true);
        Movie movie = buildMovie();
        Poster currentMain = buildPoster(POSTER_ID_1, movie, "https://img.com/old.jpg", true);
        Poster newPoster   = buildPoster(POSTER_ID_2, movie, "https://img.com/new.jpg", true);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(posterRepository.findByMovie_IdAndIsMain(MOVIE_ID, true)).thenReturn(Optional.of(currentMain));
        when(posterRepository.save(any(Poster.class))).thenReturn(newPoster);
        when(posterRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(currentMain, newPoster));

        // Act
        List<PosterResponse> result = posterService.addPoster(MOVIE_ID, request);

        // Assert — save llamado 2 veces: demotar old main y guardar new
        verify(posterRepository, times(2)).save(captor.capture());
        Poster demoted = captor.getAllValues().get(0);
        assertFalse(demoted.isMain());
        assertEquals(2, result.size());
    }

    @Test
    void testAddPosterMovieNotFound() {
        // Arrange
        CreatePosterRequest request = new CreatePosterRequest("https://img.com/p.jpg", false);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> posterService.addPoster(MOVIE_ID, request));
        verify(posterRepository, never()).save(any());
    }

    // ── setMainPoster ─────────────────────────────────────────────────────

    @Test
    void testSetMainPosterDemotesCurrentMain() throws Exception {
        // Arrange
        ArgumentCaptor<Poster> captor = ArgumentCaptor.forClass(Poster.class);
        UpdatePosterRequest request = new UpdatePosterRequest(POSTER_ID_2);
        Movie movie = buildMovie();
        Poster currentMain = buildPoster(POSTER_ID_1, movie, "https://img.com/1.jpg", true);
        Poster newMain     = buildPoster(POSTER_ID_2, movie, "https://img.com/2.jpg", false);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(posterRepository.findById(POSTER_ID_2)).thenReturn(Optional.of(newMain));
        when(posterRepository.findByMovie_IdAndIsMain(MOVIE_ID, true)).thenReturn(Optional.of(currentMain));
        when(posterRepository.save(any(Poster.class))).thenReturn(newMain);
        when(posterRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(currentMain, newMain));

        // Act
        List<PosterResponse> result = posterService.setMainPoster(MOVIE_ID, request);

        // Assert — save: demotar old main, luego promover new main
        verify(posterRepository, times(2)).save(captor.capture());
        assertFalse(captor.getAllValues().get(0).isMain()); // old → false
        assertTrue(captor.getAllValues().get(1).isMain());  // new → true
        assertEquals(2, result.size());
    }

    @Test
    void testSetMainPosterSamePosterNoOp() throws Exception {
        // Arrange
        UpdatePosterRequest request = new UpdatePosterRequest(POSTER_ID_1);
        Movie movie = buildMovie();
        Poster currentMain = buildPoster(POSTER_ID_1, movie, "https://img.com/1.jpg", true);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(posterRepository.findById(POSTER_ID_1)).thenReturn(Optional.of(currentMain));
        when(posterRepository.findByMovie_IdAndIsMain(MOVIE_ID, true)).thenReturn(Optional.of(currentMain));
        when(posterRepository.save(any(Poster.class))).thenReturn(currentMain);
        when(posterRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(currentMain));

        // Act
        posterService.setMainPoster(MOVIE_ID, request);

        // Assert — solo se llama save una vez (promover el mismo, no demotar)
        verify(posterRepository, times(1)).save(any());
    }

    @Test
    void testSetMainPosterMovieNotFound() {
        // Arrange
        UpdatePosterRequest request = new UpdatePosterRequest(POSTER_ID_2);
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> posterService.setMainPoster(MOVIE_ID, request));
        verify(posterRepository, never()).save(any());
    }

    // ── deletePoster ──────────────────────────────────────────────────────

    @Test
    void testDeleteNonMainPoster() throws Exception {
        // Arrange
        Movie movie = buildMovie();
        Poster toDelete = buildPoster(POSTER_ID_2, movie, "https://img.com/2.jpg", false);
        Poster main     = buildPoster(POSTER_ID_1, movie, "https://img.com/1.jpg", true);

        when(posterRepository.findById(POSTER_ID_2)).thenReturn(Optional.of(toDelete));
        when(posterRepository.countByMovie_Id(MOVIE_ID)).thenReturn(2L);
        when(posterRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(main));

        // Act
        List<PosterResponse> result = posterService.deletePoster(POSTER_ID_2);

        // Assert
        assertAll(
                () -> verify(posterRepository).deleteById(POSTER_ID_2),
                () -> verify(posterRepository, never()).save(any()),  // no need to reassign main
                () -> assertEquals(1, result.size())
        );
    }

    @Test
    void testDeleteMainPosterAssignsNextAsMain() throws Exception {
        // Arrange
        ArgumentCaptor<Poster> captor = ArgumentCaptor.forClass(Poster.class);
        Movie movie = buildMovie();
        Poster mainPoster = buildPoster(POSTER_ID_1, movie, "https://img.com/1.jpg", true);
        Poster other      = buildPoster(POSTER_ID_2, movie, "https://img.com/2.jpg", false);

        when(posterRepository.findById(POSTER_ID_1)).thenReturn(Optional.of(mainPoster));
        when(posterRepository.countByMovie_Id(MOVIE_ID)).thenReturn(2L);
        when(posterRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(other));

        // Act
        posterService.deletePoster(POSTER_ID_1);

        // Assert — el poster restante se promueve a main
        verify(posterRepository).deleteById(POSTER_ID_1);
        verify(posterRepository).save(captor.capture());
        assertTrue(captor.getValue().isMain());
    }

    @Test
    void testDeleteOnlyPosterThrowsConflict() {
        // Arrange
        Movie movie = buildMovie();
        Poster onlyPoster = buildPoster(POSTER_ID_1, movie, "https://img.com/1.jpg", true);

        when(posterRepository.findById(POSTER_ID_1)).thenReturn(Optional.of(onlyPoster));
        when(posterRepository.countByMovie_Id(MOVIE_ID)).thenReturn(1L);

        // Assert
        assertThrows(ConflictException.class,
                () -> posterService.deletePoster(POSTER_ID_1));
        verify(posterRepository, never()).deleteById(any());
    }

    @Test
    void testDeletePosterNotFound() {
        // Arrange
        when(posterRepository.findById(POSTER_ID_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> posterService.deletePoster(POSTER_ID_1));
        verify(posterRepository, never()).deleteById(any());
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private Movie buildMovie() {
        Movie m = new Movie();
        m.setId(MOVIE_ID);
        m.setTitle("Inception");
        return m;
    }

    private Poster buildPoster(UUID id, Movie movie, String url, boolean isMain) {
        Poster p = new Poster();
        p.setId(id);
        p.setMovie(movie);
        p.setUrlImage(url);
        p.setMain(isMain);
        return p;
    }
}
