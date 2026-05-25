package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateCastRequest;
import com.example.movies.dtos.movie.request.UpdateCastRequest;
import com.example.movies.dtos.movie.response.CastResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.CastService;
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
@WebMvcTest(CastController.class)
class CastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CastService castService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID CAST_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/movies/" + MOVIE_ID + "/cast";

    private CastResponse buildCastResponse() {
        return new CastResponse(CAST_ID, ACTOR_ID, "Tom Hanks", "http://example.com/tom.jpg", "Forrest Gump");
    }

    // ── GET /v1/movies/{movieId}/cast ──────────────────────────────────────

    @Test
    void getCast_withAuth_returnsOk() throws Exception {
        when(castService.getCast(MOVIE_ID)).thenReturn(List.of(buildCastResponse()));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actorName").value("Tom Hanks"))
                .andExpect(jsonPath("$[0].characterName").value("Forrest Gump"));
    }

    @Test
    void getCast_withoutAuth_returnsOk() throws Exception {
        when(castService.getCast(MOVIE_ID)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    void getCast_whenMovieNotFound_returnsNotFound() throws Exception {
        when(castService.getCast(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies/{movieId}/cast ─────────────────────────────────────

    @Test
    void addActor_withAdminRole_returnsCreated() throws Exception {
        CreateCastRequest request = new CreateCastRequest(ACTOR_ID, "Forrest Gump");
        when(castService.addActor(eq(MOVIE_ID), any(CreateCastRequest.class)))
                .thenReturn(List.of(buildCastResponse()));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].actorName").value("Tom Hanks"));
    }

    @Test
    void addActor_withNullActorId_returnsBadRequest() throws Exception {
        CreateCastRequest request = new CreateCastRequest(null, "Forrest Gump");

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addActor_whenMovieNotFound_returnsNotFound() throws Exception {
        CreateCastRequest request = new CreateCastRequest(ACTOR_ID, "Forrest Gump");
        when(castService.addActor(eq(MOVIE_ID), any(CreateCastRequest.class)))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addActor_whenAlreadyInCast_returnsConflict() throws Exception {
        CreateCastRequest request = new CreateCastRequest(ACTOR_ID, "Forrest Gump");
        when(castService.addActor(eq(MOVIE_ID), any(CreateCastRequest.class)))
                .thenThrow(new ConflictException("Actor already in cast"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/movies/{movieId}/cast/{castId} ───────────────────────────

    @Test
    void updateCharacterName_withAdminRole_returnsOk() throws Exception {
        UpdateCastRequest request = new UpdateCastRequest("New Character");
        CastResponse response = new CastResponse(CAST_ID, ACTOR_ID, "Tom Hanks", "http://example.com/tom.jpg", "New Character");
        when(castService.updateCharacterName(eq(CAST_ID), any(UpdateCastRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + CAST_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characterName").value("New Character"));
    }

    @Test
    void updateCharacterName_withBlankName_returnsBadRequest() throws Exception {
        UpdateCastRequest request = new UpdateCastRequest("");

        mockMvc.perform(patch(BASE_URL + "/" + CAST_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCharacterName_whenNotFound_returnsNotFound() throws Exception {
        UpdateCastRequest request = new UpdateCastRequest("New Character");
        when(castService.updateCharacterName(eq(CAST_ID), any(UpdateCastRequest.class)))
                .thenThrow(new ResourceNotFoundException("Cast not found"));

        mockMvc.perform(patch(BASE_URL + "/" + CAST_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /v1/movies/{movieId}/cast/{castId} ──────────────────────────

    @Test
    void removeActor_withAdminRole_returnsOk() throws Exception {
        when(castService.removeActor(MOVIE_ID, CAST_ID)).thenReturn(List.of());

        mockMvc.perform(delete(BASE_URL + "/" + CAST_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void removeActor_whenNotFound_returnsNotFound() throws Exception {
        when(castService.removeActor(MOVIE_ID, CAST_ID))
                .thenThrow(new ResourceNotFoundException("Cast not found"));

        mockMvc.perform(delete(BASE_URL + "/" + CAST_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeActor_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + CAST_ID))
                .andExpect(status().isUnauthorized());
    }
}
