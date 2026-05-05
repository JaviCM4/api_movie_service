package com.example.movies.services.people;

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

    // ── updatePeople ──────────────────────────────────────────────────────

    @Test
    void testUpdatePeople() throws Exception {
        // Arrange
        ArgumentCaptor<People> captor = ArgumentCaptor.forClass(People.class);
        UpdatePeopleRequest request = new UpdatePeopleRequest("Christopher Nolan");

        People existing = buildPeople("Steven Spielberg");
        People saved    = buildPeople("Christopher Nolan");

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
                () -> assertEquals("Christopher Nolan", result.getName())
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

        People existing = buildPeople("Steven Spielberg");

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
        People p1 = buildPeople("Christopher Nolan");
        People p2 = buildPeople("Steven Spielberg");
        p2.setId(UUID.randomUUID());

        when(peopleRepository.findAll()).thenReturn(List.of(p1, p2));

        // Act
        List<PeopleResponse> result = peopleService.findAll();

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Christopher Nolan", result.get(0).getName()),
                () -> assertEquals("Steven Spielberg",  result.get(1).getName())
        );
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private People buildPeople(String name) {
        People p = new People();
        p.setId(PEOPLE_ID);
        p.setName(name);
        return p;
    }
}
