package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.AssignPeopleRequest;
import com.example.movies.dtos.movie.response.MoviePeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.enums.RolMovieEnum;
import com.example.movies.services.movie.inteface.MoviePeopleService;
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
@WebMvcTest(MoviePeopleController.class)
class MoviePeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MoviePeopleService moviePeopleService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID PEOPLE_ID = UUID.randomUUID();
    private static final UUID MOVIE_PEOPLE_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/movies/" + MOVIE_ID + "/people";

    private MoviePeopleResponse buildResponse() {
        return new MoviePeopleResponse(MOVIE_PEOPLE_ID, PEOPLE_ID, "Steven Spielberg", RolMovieEnum.DIRECTOR);
    }

    // ── GET /v1/movies/{movieId}/people ────────────────────────────────────

    @Test
    void getPeople_withAuth_returnsOk() throws Exception {
        when(moviePeopleService.getPeople(MOVIE_ID)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].peopleName").value("Steven Spielberg"))
                .andExpect(jsonPath("$[0].rol").value("DIRECTOR"));
    }

    @Test
    void getPeople_withoutAuth_returnsOk() throws Exception {
        when(moviePeopleService.getPeople(MOVIE_ID)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    void getPeople_whenMovieNotFound_returnsNotFound() throws Exception {
        when(moviePeopleService.getPeople(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies/{movieId}/people ───────────────────────────────────

    @Test
    void addPerson_withAdminRole_returnsCreated() throws Exception {
        AssignPeopleRequest request = new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.DIRECTOR);
        when(moviePeopleService.addPerson(eq(MOVIE_ID), any(AssignPeopleRequest.class)))
                .thenReturn(List.of(buildResponse()));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].peopleName").value("Steven Spielberg"));
    }

    @Test
    void addPerson_withNullFields_returnsBadRequest() throws Exception {
        AssignPeopleRequest request = new AssignPeopleRequest(null, null);

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addPerson_whenAlreadyAssigned_returnsConflict() throws Exception {
        AssignPeopleRequest request = new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.DIRECTOR);
        when(moviePeopleService.addPerson(eq(MOVIE_ID), any(AssignPeopleRequest.class)))
                .thenThrow(new ConflictException("Person already assigned"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/movies/{movieId}/people/{moviePeopleId}/rol ──────────────

    @Test
    void updateRol_withAdminRole_returnsOk() throws Exception {
        MoviePeopleResponse response = new MoviePeopleResponse(MOVIE_PEOPLE_ID, PEOPLE_ID, "Steven Spielberg", RolMovieEnum.WRITER);
        when(moviePeopleService.updateRol(MOVIE_ID, MOVIE_PEOPLE_ID, RolMovieEnum.WRITER)).thenReturn(List.of(response));

        mockMvc.perform(patch(BASE_URL + "/" + MOVIE_PEOPLE_ID + "/rol")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .param("rol", "WRITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rol").value("WRITER"));
    }

    @Test
    void updateRol_whenNotFound_returnsNotFound() throws Exception {
        when(moviePeopleService.updateRol(MOVIE_ID, MOVIE_PEOPLE_ID, RolMovieEnum.WRITER))
                .thenThrow(new ResourceNotFoundException("MoviePeople not found"));

        mockMvc.perform(patch(BASE_URL + "/" + MOVIE_PEOPLE_ID + "/rol")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .param("rol", "WRITER"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /v1/movies/{movieId}/people/{moviePeopleId} ─────────────────

    @Test
    void removePerson_withAdminRole_returnsOk() throws Exception {
        when(moviePeopleService.removePerson(MOVIE_ID, MOVIE_PEOPLE_ID)).thenReturn(List.of());

        mockMvc.perform(delete(BASE_URL + "/" + MOVIE_PEOPLE_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void removePerson_whenNotFound_returnsNotFound() throws Exception {
        when(moviePeopleService.removePerson(MOVIE_ID, MOVIE_PEOPLE_ID))
                .thenThrow(new ResourceNotFoundException("MoviePeople not found"));

        mockMvc.perform(delete(BASE_URL + "/" + MOVIE_PEOPLE_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removePerson_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + MOVIE_PEOPLE_ID))
                .andExpect(status().isUnauthorized());
    }
}
