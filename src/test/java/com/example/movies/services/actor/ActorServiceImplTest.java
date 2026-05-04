package com.example.movies.services.actor;

import com.example.movies.dtos.actor.ActorResponse;
import com.example.movies.dtos.actor.CreateActorRequest;
import com.example.movies.dtos.actor.UpdateActorRequest;
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

    // Create Actor
    @Test
    void testCreateActor() throws Exception {
        // Arrange
        CreateActorRequest request = new CreateActorRequest(ACTOR_NAME, ACTOR_URL_IMAGE);
        ActorServiceImplementation spy = spy(actorService);
        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);

        when(actorRepository.existsByNameIgnoreCase(ACTOR_NAME)).thenReturn(false);

        // Act
        spy.createActor(request);

        // Assert
        assertAll(
                () -> verify(actorRepository).save(actorCaptor.capture()),
                () -> assertEquals(ACTOR_NAME, actorCaptor.getValue().getName()),
                () -> assertEquals(ACTOR_URL_IMAGE, actorCaptor.getValue().getUrlImage())
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
    }

    // Update Actor
    @Test
    void testUpdateActor() throws Exception {
        // Arrange
        UpdateActorRequest request = new UpdateActorRequest(ACTOR_NAME_UPDATED, ACTOR_URL_IMAGE_UPDATED);
        ActorServiceImplementation spy = spy(actorService);

        Actor existingActor = new Actor();
        existingActor.setName(ACTOR_NAME);
        existingActor.setUrlImage(ACTOR_URL_IMAGE);

        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);

        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(existingActor));
        when(actorRepository.existsByNameIgnoreCaseAndIdNot(ACTOR_NAME_UPDATED, ACTOR_ID)).thenReturn(false);

        // Act
        spy.updateActor(ACTOR_ID, request);

        // Assert
        assertAll(
                () -> verify(actorRepository).save(actorCaptor.capture()),
                () -> assertEquals(ACTOR_NAME_UPDATED, actorCaptor.getValue().getName()),
                () -> assertEquals(ACTOR_URL_IMAGE_UPDATED, actorCaptor.getValue().getUrlImage())
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
    }

    @Test
    void testUpdateActorWhenDuplicatedName() {
        // Arrange
        UpdateActorRequest request = new UpdateActorRequest(ACTOR_NAME_UPDATED, ACTOR_URL_IMAGE_UPDATED);

        Actor existingActor = new Actor();
        existingActor.setName(ACTOR_NAME);

        when(actorRepository.findById(ACTOR_ID)).thenReturn(Optional.of(existingActor));
        when(actorRepository.existsByNameIgnoreCaseAndIdNot(ACTOR_NAME_UPDATED, ACTOR_ID)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> actorService.updateActor(ACTOR_ID, request));
    }

    // find All Actor
    @Test
    void testFindAllActors() {
        // Arrange
        Actor actor1 = new Actor();
        actor1.setName(ACTOR_NAME);
        actor1.setUrlImage(ACTOR_URL_IMAGE);

        Actor actor2 = new Actor();
        actor2.setName("Brad Pitt");
        actor2.setUrlImage("http://img.com/brad.jpg");

        when(actorRepository.findAll()).thenReturn(List.of(actor1, actor2));

        // Act
        List<ActorResponse> result = actorService.findAllActor();

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(ACTOR_NAME, result.get(0).getName()),
                () -> assertEquals(ACTOR_URL_IMAGE, result.get(0).getUrlImage()),
                () -> assertEquals("Brad Pitt", result.get(1).getName())
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
}
