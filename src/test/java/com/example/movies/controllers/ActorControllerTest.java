package com.example.movies.controllers;

import com.example.movies.dtos.actor.request.CreateActorRequest;
import com.example.movies.dtos.actor.request.UpdateActorRequest;
import com.example.movies.dtos.actor.response.ActorResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.actor.ActorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.movies.security.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com")
@Import(SecurityConfig.class)
@WebMvcTest(ActorController.class)
class ActorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActorService actorService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/actors";

    private ActorResponse buildActorResponse(boolean active) {
        return new ActorResponse(ACTOR_ID, "Tom Hanks", "http://example.com/tom.jpg", active);
    }

    // ── GET /v1/actors ─────────────────────────────────────────────────────

    @Test
    void listActors_withAdminRole_returnsOk() throws Exception {
        when(actorService.findAllActor()).thenReturn(List.of(buildActorResponse(true)));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tom Hanks"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void listActors_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listActors_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ── POST /v1/actors ────────────────────────────────────────────────────

    @Test
    void createActor_withAdminRole_returnsCreated() throws Exception {
        CreateActorRequest request = new CreateActorRequest("Tom Hanks", "http://example.com/tom.jpg");
        when(actorService.createActor(any(CreateActorRequest.class))).thenReturn(buildActorResponse(true));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tom Hanks"));
    }

    @Test
    void createActor_withBlankName_returnsBadRequest() throws Exception {
        CreateActorRequest request = new CreateActorRequest("", null);

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createActor_whenDuplicated_returnsConflict() throws Exception {
        CreateActorRequest request = new CreateActorRequest("Tom Hanks", "http://example.com/tom.jpg");
        when(actorService.createActor(any(CreateActorRequest.class)))
                .thenThrow(new ConflictException("Actor already exists"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/actors/{id} ──────────────────────────────────────────────

    @Test
    void updateActor_withAdminRole_returnsOk() throws Exception {
        UpdateActorRequest request = new UpdateActorRequest("Tom Hanks Updated", "http://example.com/tom2.jpg");
        ActorResponse response = new ActorResponse(ACTOR_ID, "Tom Hanks Updated", "http://example.com/tom2.jpg", true);
        when(actorService.updateActor(eq(ACTOR_ID), any(UpdateActorRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + ACTOR_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tom Hanks Updated"));
    }

    @Test
    void updateActor_whenNotFound_returnsNotFound() throws Exception {
        UpdateActorRequest request = new UpdateActorRequest("Tom Hanks Updated", null);
        when(actorService.updateActor(eq(ACTOR_ID), any(UpdateActorRequest.class)))
                .thenThrow(new ResourceNotFoundException("Actor not found"));

        mockMvc.perform(patch(BASE_URL + "/" + ACTOR_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActor_whenDuplicated_returnsConflict() throws Exception {
        UpdateActorRequest request = new UpdateActorRequest("Tom Hanks Duplicated", null);
        when(actorService.updateActor(eq(ACTOR_ID), any(UpdateActorRequest.class)))
                .thenThrow(new ConflictException("Name already in use"));

        mockMvc.perform(patch(BASE_URL + "/" + ACTOR_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/actors/{id}/toggle ───────────────────────────────────────

    @Test
    void toggleActor_withAdminRole_returnsOk() throws Exception {
        ActorResponse response = buildActorResponse(false);
        when(actorService.toggleActor(ACTOR_ID)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + ACTOR_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleActor_whenNotFound_returnsNotFound() throws Exception {
        when(actorService.toggleActor(ACTOR_ID))
                .thenThrow(new ResourceNotFoundException("Actor not found"));

        mockMvc.perform(patch(BASE_URL + "/" + ACTOR_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}
