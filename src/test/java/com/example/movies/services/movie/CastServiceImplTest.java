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
public class CastServiceImplTest {

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID CAST_ID  = UUID.randomUUID();

    @Mock private MovieRepository movieRepository;
    @Mock private ActorRepository actorRepository;
    @Mock private CastRepository  castRepository;

    @InjectMocks
    private CastServiceImplementation castService;

    // ── addActor ──────────────────────────────────────────────────────────

    @Test
    void testAddActor() throws Exception {
        // Arrange
        ArgumentCaptor<Cast> captor = ArgumentCaptor.forClass(Cast.class);
        Movie movie = buildMovie();
        Actor actor = buildActor("Leonardo DiCaprio");
        Cast  saved = buildCast(movie, actor, "Dom Cobb");

        CreateCastRequest request = new CreateCastRequest(ACTOR_ID, "Dom Cobb");

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(actor));
        when(castRepository.existsByMovie_IdAndActor_Id(MOVIE_ID, ACTOR_ID)).thenReturn(false);
        when(castRepository.save(any(Cast.class))).thenReturn(saved);
        when(castRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(saved));

        // Act
        List<CastResponse> result = castService.addActor(MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(castRepository).save(captor.capture()),
                () -> assertEquals(movie, captor.getValue().getMovie()),
                () -> assertEquals(actor, captor.getValue().getActor()),
                () -> assertEquals("Dom Cobb", captor.getValue().getCharacterName()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Leonardo DiCaprio", result.get(0).getActorName()),
                () -> assertEquals("Dom Cobb", result.get(0).getCharacterName())
        );
    }

    @Test
    void testAddActorWhenMovieNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> castService.addActor(MOVIE_ID, new CreateCastRequest(ACTOR_ID, "Dom Cobb")));
        verify(castRepository, never()).save(any());
    }

    @Test
    void testAddActorWhenActorNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> castService.addActor(MOVIE_ID, new CreateCastRequest(ACTOR_ID, "Dom Cobb")));
        verify(castRepository, never()).save(any());
    }

    @Test
    void testAddActorWhenAlreadyInCast() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(buildActor("Leonardo DiCaprio")));
        when(castRepository.existsByMovie_IdAndActor_Id(MOVIE_ID, ACTOR_ID)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> castService.addActor(MOVIE_ID, new CreateCastRequest(ACTOR_ID, "Dom Cobb")));
        verify(castRepository, never()).save(any());
    }

    // ── updateCharacterName ───────────────────────────────────────────────

    @Test
    void testUpdateCharacterName() throws Exception {
        // Arrange
        ArgumentCaptor<Cast> captor = ArgumentCaptor.forClass(Cast.class);
        Actor actor = buildActor("Leonardo DiCaprio");
        Cast  cast  = buildCast(buildMovie(), actor, "Old Name");
        Cast  saved = buildCast(buildMovie(), actor, "New Name");

        UpdateCastRequest request = new UpdateCastRequest("New Name");

        when(castRepository.findById(CAST_ID)).thenReturn(Optional.of(cast));
        when(castRepository.save(any(Cast.class))).thenReturn(saved);

        // Act
        CastResponse result = castService.updateCharacterName(CAST_ID, request);

        // Assert
        assertAll(
                () -> verify(castRepository).save(captor.capture()),
                () -> assertEquals("New Name", captor.getValue().getCharacterName()),
                () -> assertEquals("New Name", result.getCharacterName()),
                () -> assertEquals("Leonardo DiCaprio", result.getActorName())
        );
    }

    @Test
    void testUpdateCharacterNameWhenCastNotFound() {
        // Arrange
        when(castRepository.findById(CAST_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> castService.updateCharacterName(CAST_ID, new UpdateCastRequest("New Name")));
        verify(castRepository, never()).save(any());
    }

    // ── removeActor ───────────────────────────────────────────────────────

    @Test
    void testRemoveActor() throws Exception {
        // Arrange
        Movie movie = buildMovie();
        Actor actor = buildActor("Leonardo DiCaprio");
        Cast  cast  = buildCast(movie, actor, "Dom Cobb");

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(castRepository.findById(CAST_ID)).thenReturn(Optional.of(cast));
        when(castRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of());

        // Act
        List<CastResponse> result = castService.removeActor(MOVIE_ID, CAST_ID);

        // Assert
        assertAll(
                () -> verify(castRepository).delete(cast),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void testRemoveActorWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> castService.removeActor(MOVIE_ID, CAST_ID));
        verify(castRepository, never()).delete(any());
    }

    @Test
    void testRemoveActorWhenCastNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(castRepository.findById(CAST_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> castService.removeActor(MOVIE_ID, CAST_ID));
        verify(castRepository, never()).delete(any());
    }

    // ── getCast ───────────────────────────────────────────────────────────

    @Test
    void testGetCast() throws Exception {
        // Arrange
        Movie movie  = buildMovie();
        Actor actor1 = buildActor("Leonardo DiCaprio");
        Actor actor2 = buildActor("Joseph Gordon-Levitt");
        actor2.setId(UUID.randomUUID());

        Cast cast1 = buildCast(movie, actor1, "Dom Cobb");
        Cast cast2 = buildCast(movie, actor2, "Arthur");

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(castRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(cast1, cast2));

        // Act
        List<CastResponse> result = castService.getCast(MOVIE_ID);

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Leonardo DiCaprio",     result.get(0).getActorName()),
                () -> assertEquals("Dom Cobb",              result.get(0).getCharacterName()),
                () -> assertEquals("Joseph Gordon-Levitt",  result.get(1).getActorName()),
                () -> assertEquals("Arthur",                result.get(1).getCharacterName())
        );
    }

    @Test
    void testGetCastWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> castService.getCast(MOVIE_ID));
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Movie buildMovie() {
        Movie movie = new Movie();
        movie.setId(MOVIE_ID);
        movie.setTitle("Inception");
        return movie;
    }

    private Actor buildActor(String name) {
        Actor actor = new Actor();
        actor.setId(ACTOR_ID);
        actor.setName(name);
        return actor;
    }

    private Cast buildCast(Movie movie, Actor actor, String characterName) {
        Cast cast = new Cast();
        cast.setId(CAST_ID);
        cast.setMovie(movie);
        cast.setActor(actor);
        cast.setCharacterName(characterName);
        return cast;
    }
}
