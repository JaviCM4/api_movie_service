package com.example.movies.services.people;

import com.example.movies.dtos.people.request.CreatePeopleRequest;
import com.example.movies.dtos.people.request.UpdatePeopleRequest;
import com.example.movies.dtos.people.response.PeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.people.People;
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
public class PeopleServiceImplTest {

    private static final UUID PEOPLE_ID = UUID.randomUUID();

    @Mock private PeopleRepository peopleRepository;

    @InjectMocks
    private PeopleServiceImplementation peopleService;

    // ── createPeople ──────────────────────────────────────────────────────

    @Test
    void testCreatePeople() throws Exception {
        // Arrange
        ArgumentCaptor<People> captor = ArgumentCaptor.forClass(People.class);
        CreatePeopleRequest request = new CreatePeopleRequest("Christopher Nolan");
        People saved = buildPeople(PEOPLE_ID, "Christopher Nolan", true);

        when(peopleRepository.existsByNameIgnoreCase("Christopher Nolan")).thenReturn(false);
        when(peopleRepository.save(any(People.class))).thenReturn(saved);

        // Act
        PeopleResponse result = peopleService.createPeople(request);

        // Assert
        assertAll(
                () -> verify(peopleRepository).save(captor.capture()),
                () -> assertEquals("Christopher Nolan", captor.getValue().getName()),
                () -> assertEquals("Christopher Nolan", result.getName()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testCreatePeopleWhenDuplicatedName() {
        // Arrange
        CreatePeopleRequest request = new CreatePeopleRequest("Christopher Nolan");
        when(peopleRepository.existsByNameIgnoreCase("Christopher Nolan")).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> peopleService.createPeople(request));
        verify(peopleRepository, never()).save(any());
    }

    // ── updatePeople ──────────────────────────────────────────────────────

    @Test
    void testUpdatePeople() throws Exception {
        // Arrange
        ArgumentCaptor<People> captor = ArgumentCaptor.forClass(People.class);
        UpdatePeopleRequest request = new UpdatePeopleRequest("Christopher Nolan");

        People existing = buildPeople(PEOPLE_ID, "Steven Spielberg", true);
        People saved    = buildPeople(PEOPLE_ID, "Christopher Nolan", true);

        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.of(existing));
        when(peopleRepository.existsByNameIgnoreCaseAndIdNot("Christopher Nolan", PEOPLE_ID)).thenReturn(false);
        when(peopleRepository.save(any(People.class))).thenReturn(saved);

        // Act
        PeopleResponse result = peopleService.updatePeople(PEOPLE_ID, request);

        // Assert
        assertAll(
                () -> verify(peopleRepository).save(captor.capture()),
                () -> assertEquals("Christopher Nolan", captor.getValue().getName()),
                () -> assertEquals(PEOPLE_ID, result.getId()),
                () -> assertEquals("Christopher Nolan", result.getName()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testUpdatePeopleWhenNotFound() {
        // Arrange
        UpdatePeopleRequest request = new UpdatePeopleRequest("Christopher Nolan");
        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> peopleService.updatePeople(PEOPLE_ID, request));
        verify(peopleRepository, never()).save(any());
    }

    @Test
    void testUpdatePeopleWhenNameConflict() {
        // Arrange
        UpdatePeopleRequest request = new UpdatePeopleRequest("Christopher Nolan");
        People existing = buildPeople(PEOPLE_ID, "Steven Spielberg", true);

        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.of(existing));
        when(peopleRepository.existsByNameIgnoreCaseAndIdNot("Christopher Nolan", PEOPLE_ID)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> peopleService.updatePeople(PEOPLE_ID, request));
        verify(peopleRepository, never()).save(any());
    }

    // ── findAll ───────────────────────────────────────────────────────────

    @Test
    void testFindAll() {
        // Arrange
        People p1 = buildPeople(PEOPLE_ID, "Christopher Nolan", true);
        People p2 = buildPeople(UUID.randomUUID(), "Steven Spielberg", false);

        when(peopleRepository.findAll()).thenReturn(List.of(p1, p2));

        // Act
        List<PeopleResponse> result = peopleService.findAll();

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Christopher Nolan", result.get(0).getName()),
                () -> assertTrue(result.get(0).isActive()),
                () -> assertEquals("Steven Spielberg",  result.get(1).getName()),
                () -> assertFalse(result.get(1).isActive())
        );
    }

    @Test
    void testFindAllWhenEmpty() {
        // Arrange
        when(peopleRepository.findAll()).thenReturn(List.of());

        // Act
        List<PeopleResponse> result = peopleService.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ── togglePeople ──────────────────────────────────────────────────────

    @Test
    void testTogglePeopleDeactivates() throws Exception {
        // Arrange
        People active = buildPeople(PEOPLE_ID, "Christopher Nolan", true);
        People saved  = buildPeople(PEOPLE_ID, "Christopher Nolan", false);

        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.of(active));
        when(peopleRepository.save(any(People.class))).thenReturn(saved);

        // Act
        PeopleResponse result = peopleService.togglePeople(PEOPLE_ID);

        // Assert
        assertAll(
                () -> verify(peopleRepository).save(active),
                () -> assertFalse(result.isActive())
        );
    }

    @Test
    void testTogglePeopleActivates() throws Exception {
        // Arrange
        People inactive = buildPeople(PEOPLE_ID, "Christopher Nolan", false);
        People saved    = buildPeople(PEOPLE_ID, "Christopher Nolan", true);

        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.of(inactive));
        when(peopleRepository.save(any(People.class))).thenReturn(saved);

        // Act
        PeopleResponse result = peopleService.togglePeople(PEOPLE_ID);

        // Assert
        assertAll(
                () -> verify(peopleRepository).save(inactive),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testTogglePeopleWhenNotFound() {
        // Arrange
        when(peopleRepository.findById(PEOPLE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> peopleService.togglePeople(PEOPLE_ID));
        verify(peopleRepository, never()).save(any());
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private People buildPeople(UUID id, String name, boolean active) {
        People p = new People();
        p.setId(id);
        p.setName(name);
        p.setActive(active);
        return p;
    }
}
