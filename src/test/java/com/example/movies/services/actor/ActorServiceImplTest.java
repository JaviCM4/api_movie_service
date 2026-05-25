package com.example.movies.services.actor;

import com.example.movies.dtos.actor.response.ActorResponse;
import com.example.movies.dtos.actor.request.CreateActorRequest;
import com.example.movies.dtos.actor.request.UpdateActorRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.actor.Actor;
import com.example.movies.repositories.actor.ActorRepository;
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
public class ActorServiceImplTest {

    private static final String ACTOR_NAME = "Tom Hanks";
    private static final String ACTOR_URL_IMAGE = "http://img.com/tom.jpg";
    private static final String ACTOR_NAME_UPDATED = "Tom Hanks Updated";
    private static final String ACTOR_URL_IMAGE_UPDATED = "http://img.com/tom2.jpg";
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @Mock
    private ActorRepository actorRepository;

    @InjectMocks
    private ActorServiceImplementation actorService;

    // ── createActor ───────────────────────────────────────────────────────

    @Test
    void testCreateActor() throws Exception {
        // Arrange
        CreateActorRequest request = new CreateActorRequest(ACTOR_NAME, ACTOR_URL_IMAGE);
        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);
        Actor saved = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, true);

        when(actorRepository.existsByNameIgnoreCase(ACTOR_NAME)).thenReturn(false);
        when(actorRepository.save(any(Actor.class))).thenReturn(saved);

        // Act
        ActorResponse result = actorService.createActor(request);

        // Assert
        assertAll(
                () -> verify(actorRepository).save(actorCaptor.capture()),
                () -> assertEquals(ACTOR_NAME, actorCaptor.getValue().getName()),
                () -> assertEquals(ACTOR_URL_IMAGE, actorCaptor.getValue().getUrlImage()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testCreateActorWhenDuplicatedName() {
        // Arrange
        CreateActorRequest request = new CreateActorRequest(ACTOR_NAME, ACTOR_URL_IMAGE);

        when(actorRepository.existsByNameIgnoreCase(ACTOR_NAME)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> actorService.createActor(request));
        verify(actorRepository, never()).save(any());
    }

    // ── updateActor ───────────────────────────────────────────────────────

    @Test
    void testUpdateActor() throws Exception {
        // Arrange
        UpdateActorRequest request = new UpdateActorRequest(ACTOR_NAME_UPDATED, ACTOR_URL_IMAGE_UPDATED);
        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);

        Actor existing = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, true);
        Actor saved    = buildActor(ACTOR_ID, ACTOR_NAME_UPDATED, ACTOR_URL_IMAGE_UPDATED, true);

        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(existing));
        when(actorRepository.existsByNameIgnoreCaseAndIdNot(ACTOR_NAME_UPDATED, ACTOR_ID)).thenReturn(false);
        when(actorRepository.save(any(Actor.class))).thenReturn(saved);

        // Act
        ActorResponse result = actorService.updateActor(ACTOR_ID, request);

        // Assert
        assertAll(
                () -> verify(actorRepository).save(actorCaptor.capture()),
                () -> assertEquals(ACTOR_NAME_UPDATED, actorCaptor.getValue().getName()),
                () -> assertEquals(ACTOR_URL_IMAGE_UPDATED, actorCaptor.getValue().getUrlImage()),
                () -> assertEquals(ACTOR_NAME_UPDATED, result.getName()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testUpdateActorWhenActorNotFound() {
        // Arrange
        UpdateActorRequest request = new UpdateActorRequest(ACTOR_NAME_UPDATED, ACTOR_URL_IMAGE_UPDATED);

        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> actorService.updateActor(ACTOR_ID, request));
        verify(actorRepository, never()).save(any());
    }

    @Test
    void testUpdateActorWhenDuplicatedName() {
        // Arrange
        UpdateActorRequest request = new UpdateActorRequest(ACTOR_NAME_UPDATED, ACTOR_URL_IMAGE_UPDATED);

        Actor existing = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, true);

        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(existing));
        when(actorRepository.existsByNameIgnoreCaseAndIdNot(ACTOR_NAME_UPDATED, ACTOR_ID)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> actorService.updateActor(ACTOR_ID, request));
        verify(actorRepository, never()).save(any());
    }

    // ── findAllActor ──────────────────────────────────────────────────────

    @Test
    void testFindAllActors() {
        // Arrange
        Actor actor1 = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, true);
        Actor actor2 = buildActor(UUID.randomUUID(), "Brad Pitt", "http://img.com/brad.jpg", false);

        when(actorRepository.findAll()).thenReturn(List.of(actor1, actor2));

        // Act
        List<ActorResponse> result = actorService.findAllActor();

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(ACTOR_NAME, result.get(0).getName()),
                () -> assertEquals(ACTOR_URL_IMAGE, result.get(0).getUrlImage()),
                () -> assertTrue(result.get(0).isActive()),
                () -> assertEquals("Brad Pitt", result.get(1).getName()),
                () -> assertFalse(result.get(1).isActive())
        );
    }

    @Test
    void testFindAllActorsWhenEmpty() {
        // Arrange
        when(actorRepository.findAll()).thenReturn(List.of());

        // Act
        List<ActorResponse> result = actorService.findAllActor();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ── toggleActor ───────────────────────────────────────────────────────

    @Test
    void testToggleActorDeactivates() throws Exception {
        // Arrange
        Actor active = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, true);
        Actor saved  = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, false);

        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(active));
        when(actorRepository.save(any(Actor.class))).thenReturn(saved);

        // Act
        ActorResponse result = actorService.toggleActor(ACTOR_ID);

        // Assert
        assertAll(
                () -> verify(actorRepository).save(active),
                () -> assertFalse(result.isActive())
        );
    }

    @Test
    void testToggleActorActivates() throws Exception {
        // Arrange
        Actor inactive = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, false);
        Actor saved    = buildActor(ACTOR_ID, ACTOR_NAME, ACTOR_URL_IMAGE, true);

        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(inactive));
        when(actorRepository.save(any(Actor.class))).thenReturn(saved);

        // Act
        ActorResponse result = actorService.toggleActor(ACTOR_ID);

        // Assert
        assertAll(
                () -> verify(actorRepository).save(inactive),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testToggleActorWhenNotFound() {
        // Arrange
        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> actorService.toggleActor(ACTOR_ID));
        verify(actorRepository, never()).save(any());
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private Actor buildActor(UUID id, String name, String urlImage, boolean active) {
        Actor actor = new Actor();
        actor.setId(id);
        actor.setName(name);
        actor.setUrlImage(urlImage);
        actor.setActive(active);
        return actor;
    }
}
