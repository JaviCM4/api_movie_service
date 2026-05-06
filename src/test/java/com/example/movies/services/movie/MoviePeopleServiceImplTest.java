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
public class MoviePeopleServiceImplTest {

    private static final UUID MOVIE_ID        = UUID.randomUUID();
    private static final UUID PEOPLE_ID       = UUID.randomUUID();
    private static final UUID MOVIE_PEOPLE_ID = UUID.randomUUID();

    @Mock private MovieRepository       movieRepository;
    @Mock private PeopleRepository      peopleRepository;
    @Mock private MoviePeopleRepository moviePeopleRepository;

    @InjectMocks
    private MoviePeopleServiceImplementation moviePeopleService;

    // ── Helpers ───────────────────────────────────────────────────────────

    private Movie buildMovie() {
        Movie m = new Movie();
        m.setId(MOVIE_ID);
        m.setTitle("Inception");
        return m;
    }

    private People buildPeople(String name) {
        People p = new People();
        p.setId(PEOPLE_ID);
        p.setName(name);
        return p;
    }

    private MoviePeople buildMoviePeople(Movie movie, People people, RolMovieEnum rol) {
        MoviePeople mp = new MoviePeople();
        mp.setId(MOVIE_PEOPLE_ID);
        mp.setMovie(movie);
        mp.setPeople(people);
        mp.setRol(rol);
        return mp;
    }

    // ── addPerson ─────────────────────────────────────────────────────────

    @Test
    void testAddPerson() throws Exception {
        // Arrange
        ArgumentCaptor<MoviePeople> captor = ArgumentCaptor.forClass(MoviePeople.class);
        Movie movie = buildMovie();
        People people = buildPeople("Christopher Nolan");
        MoviePeople saved = buildMoviePeople(movie, people, RolMovieEnum.DIRECTOR);

        AssignPeopleRequest request = new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.DIRECTOR);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.of(people));
        when(moviePeopleRepository.existsByMovie_IdAndPeople_IdAndRol(MOVIE_ID, PEOPLE_ID, RolMovieEnum.DIRECTOR)).thenReturn(false);
        when(moviePeopleRepository.save(any(MoviePeople.class))).thenReturn(saved);
        when(moviePeopleRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(saved));

        // Act
        List<MoviePeopleResponse> result = moviePeopleService.addPerson(MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(moviePeopleRepository).save(captor.capture()),
                () -> assertEquals(movie, captor.getValue().getMovie()),
                () -> assertEquals(people, captor.getValue().getPeople()),
                () -> assertEquals(RolMovieEnum.DIRECTOR, captor.getValue().getRol()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Christopher Nolan", result.get(0).getPeopleName()),
                () -> assertEquals(RolMovieEnum.DIRECTOR, result.get(0).getRol())
        );
    }

    @Test
    void testAddPersonWhenMovieNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> moviePeopleService.addPerson(MOVIE_ID, new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.DIRECTOR)));
        verify(moviePeopleRepository, never()).save(any());
    }

    @Test
    void testAddPersonWhenPeopleNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> moviePeopleService.addPerson(MOVIE_ID, new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.DIRECTOR)));
        verify(moviePeopleRepository, never()).save(any());
    }

    @Test
    void testAddPersonWhenRolAlreadyAssigned() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.of(buildPeople("Christopher Nolan")));
        when(moviePeopleRepository.existsByMovie_IdAndPeople_IdAndRol(MOVIE_ID, PEOPLE_ID, RolMovieEnum.DIRECTOR)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> moviePeopleService.addPerson(MOVIE_ID, new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.DIRECTOR)));
        verify(moviePeopleRepository, never()).save(any());
    }

    @Test
    void testAddPersonSamePersonDifferentRolAllowed() throws Exception {
        // Arrange — misma persona, pero diferente rol (WRITER), debe guardarse
        Movie movie = buildMovie();
        People people = buildPeople("Christopher Nolan");
        MoviePeople saved = buildMoviePeople(movie, people, RolMovieEnum.WRITER);

        AssignPeopleRequest request = new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.WRITER);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.of(people));
        when(moviePeopleRepository.existsByMovie_IdAndPeople_IdAndRol(MOVIE_ID, PEOPLE_ID, RolMovieEnum.WRITER)).thenReturn(false);
        when(moviePeopleRepository.save(any(MoviePeople.class))).thenReturn(saved);
        when(moviePeopleRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(saved));

        // Act
        List<MoviePeopleResponse> result = moviePeopleService.addPerson(MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(moviePeopleRepository).save(any(MoviePeople.class)),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(RolMovieEnum.WRITER, result.get(0).getRol())
        );
    }

    // ── updateRol ─────────────────────────────────────────────────────────

    @Test
    void testUpdateRol() throws Exception {
        // Arrange
        ArgumentCaptor<MoviePeople> captor = ArgumentCaptor.forClass(MoviePeople.class);
        Movie movie = buildMovie();
        People people = buildPeople("Christopher Nolan");
        MoviePeople moviePeople = buildMoviePeople(movie, people, RolMovieEnum.DIRECTOR);
        MoviePeople updated = buildMoviePeople(movie, people, RolMovieEnum.PRODUCER);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(moviePeopleRepository.findById(MOVIE_PEOPLE_ID)).thenReturn(Optional.of(moviePeople));
        when(moviePeopleRepository.existsByMovie_IdAndPeople_IdAndRol(MOVIE_ID, PEOPLE_ID, RolMovieEnum.PRODUCER)).thenReturn(false);
        when(moviePeopleRepository.save(any(MoviePeople.class))).thenReturn(updated);
        when(moviePeopleRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(updated));

        // Act
        List<MoviePeopleResponse> result = moviePeopleService.updateRol(MOVIE_ID, MOVIE_PEOPLE_ID, RolMovieEnum.PRODUCER);

        // Assert
        assertAll(
                () -> verify(moviePeopleRepository).save(captor.capture()),
                () -> assertEquals(RolMovieEnum.PRODUCER, captor.getValue().getRol()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(RolMovieEnum.PRODUCER, result.get(0).getRol())
        );
    }

    @Test
    void testUpdateRolWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> moviePeopleService.updateRol(MOVIE_ID, MOVIE_PEOPLE_ID, RolMovieEnum.PRODUCER));
        verify(moviePeopleRepository, never()).save(any());
    }

    @Test
    void testUpdateRolWhenEntryNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(moviePeopleRepository.findById(MOVIE_PEOPLE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> moviePeopleService.updateRol(MOVIE_ID, MOVIE_PEOPLE_ID, RolMovieEnum.PRODUCER));
        verify(moviePeopleRepository, never()).save(any());
    }

    @Test
    void testUpdateRolWhenRolAlreadyAssigned() {
        // Arrange
        Movie movie = buildMovie();
        People people = buildPeople("Christopher Nolan");
        MoviePeople moviePeople = buildMoviePeople(movie, people, RolMovieEnum.DIRECTOR);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(moviePeopleRepository.findById(MOVIE_PEOPLE_ID)).thenReturn(Optional.of(moviePeople));
        when(moviePeopleRepository.existsByMovie_IdAndPeople_IdAndRol(MOVIE_ID, PEOPLE_ID, RolMovieEnum.WRITER)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> moviePeopleService.updateRol(MOVIE_ID, MOVIE_PEOPLE_ID, RolMovieEnum.WRITER));
        verify(moviePeopleRepository, never()).save(any());
    }

    // ── removePerson ──────────────────────────────────────────────────────

    @Test
    void testRemovePerson() throws Exception {
        // Arrange
        Movie movie = buildMovie();
        People people = buildPeople("Christopher Nolan");
        MoviePeople moviePeople = buildMoviePeople(movie, people, RolMovieEnum.DIRECTOR);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(moviePeopleRepository.findById(MOVIE_PEOPLE_ID)).thenReturn(Optional.of(moviePeople));
        when(moviePeopleRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of());

        // Act
        List<MoviePeopleResponse> result = moviePeopleService.removePerson(MOVIE_ID, MOVIE_PEOPLE_ID);

        // Assert
        assertAll(
                () -> verify(moviePeopleRepository).delete(moviePeople),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void testRemovePersonWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> moviePeopleService.removePerson(MOVIE_ID, MOVIE_PEOPLE_ID));
        verify(moviePeopleRepository, never()).delete(any());
    }

    @Test
    void testRemovePersonWhenEntryNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(moviePeopleRepository.findById(MOVIE_PEOPLE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> moviePeopleService.removePerson(MOVIE_ID, MOVIE_PEOPLE_ID));
        verify(moviePeopleRepository, never()).delete(any());
    }

    // ── getPeople ─────────────────────────────────────────────────────────

    @Test
    void testGetPeople() throws Exception {
        // Arrange
        Movie movie = buildMovie();
        People people = buildPeople("Christopher Nolan");
        MoviePeople mp = buildMoviePeople(movie, people, RolMovieEnum.DIRECTOR);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(moviePeopleRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(mp));

        // Act
        List<MoviePeopleResponse> result = moviePeopleService.getPeople(MOVIE_ID);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(MOVIE_PEOPLE_ID, result.get(0).getMoviePeopleId()),
                () -> assertEquals(PEOPLE_ID, result.get(0).getPeopleId()),
                () -> assertEquals("Christopher Nolan", result.get(0).getPeopleName()),
                () -> assertEquals(RolMovieEnum.DIRECTOR, result.get(0).getRol())
        );
    }

    @Test
    void testGetPeopleWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> moviePeopleService.getPeople(MOVIE_ID));
    }
}
