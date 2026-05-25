package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreatePosterRequest;
import com.example.movies.dtos.movie.request.UpdatePosterRequest;
import com.example.movies.dtos.movie.response.PosterResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.PosterService;
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
@WebMvcTest(PosterController.class)
class PosterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PosterService posterService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID POSTER_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/movies/" + MOVIE_ID + "/posters";

    private PosterResponse buildPosterResponse(boolean main) {
        return new PosterResponse(POSTER_ID, "http://example.com/poster.jpg", main);
    }

    // ── GET /v1/movies/{movieId}/posters ───────────────────────────────────

    @Test
    void getPosters_withAdminRole_returnsOk() throws Exception {
        when(posterService.getPosters(MOVIE_ID)).thenReturn(List.of(buildPosterResponse(true)));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].urlImage").value("http://example.com/poster.jpg"))
                .andExpect(jsonPath("$[0].main").value(true));
    }

    @Test
    void getPosters_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPosters_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPosters_whenMovieNotFound_returnsNotFound() throws Exception {
        when(posterService.getPosters(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies/{movieId}/posters ──────────────────────────────────

    @Test
    void addPoster_withAdminRole_returnsCreated() throws Exception {
        CreatePosterRequest request = new CreatePosterRequest("http://example.com/poster.jpg", true);
        when(posterService.addPoster(eq(MOVIE_ID), any(CreatePosterRequest.class)))
                .thenReturn(List.of(buildPosterResponse(true)));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].urlImage").value("http://example.com/poster.jpg"));
    }

    @Test
    void addPoster_withInvalidUrl_returnsBadRequest() throws Exception {
        CreatePosterRequest request = new CreatePosterRequest("not-a-url", true);

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addPoster_whenConflict_returnsConflict() throws Exception {
        CreatePosterRequest request = new CreatePosterRequest("http://example.com/poster.jpg", true);
        when(posterService.addPoster(eq(MOVIE_ID), any(CreatePosterRequest.class)))
                .thenThrow(new ConflictException("Poster already exists"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/movies/{movieId}/posters/main ────────────────────────────

    @Test
    void setMainPoster_withAdminRole_returnsOk() throws Exception {
        UpdatePosterRequest request = new UpdatePosterRequest(POSTER_ID);
        when(posterService.setMainPoster(eq(MOVIE_ID), any(UpdatePosterRequest.class)))
                .thenReturn(List.of(buildPosterResponse(true)));

        mockMvc.perform(patch(BASE_URL + "/main")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].main").value(true));
    }

    @Test
    void setMainPoster_withNullId_returnsBadRequest() throws Exception {
        UpdatePosterRequest request = new UpdatePosterRequest(null);

        mockMvc.perform(patch(BASE_URL + "/main")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setMainPoster_whenNotFound_returnsNotFound() throws Exception {
        UpdatePosterRequest request = new UpdatePosterRequest(POSTER_ID);
        when(posterService.setMainPoster(eq(MOVIE_ID), any(UpdatePosterRequest.class)))
                .thenThrow(new ResourceNotFoundException("Poster not found"));

        mockMvc.perform(patch(BASE_URL + "/main")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /v1/movies/{movieId}/posters/{posterId} ─────────────────────

    @Test
    void deletePoster_withAdminRole_returnsOk() throws Exception {
        when(posterService.deletePoster(POSTER_ID)).thenReturn(List.of());

        mockMvc.perform(delete(BASE_URL + "/" + POSTER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void deletePoster_whenNotFound_returnsNotFound() throws Exception {
        when(posterService.deletePoster(POSTER_ID))
                .thenThrow(new ResourceNotFoundException("Poster not found"));

        mockMvc.perform(delete(BASE_URL + "/" + POSTER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePoster_whenConflict_returnsConflict() throws Exception {
        when(posterService.deletePoster(POSTER_ID))
                .thenThrow(new ConflictException("Cannot delete the main poster"));

        mockMvc.perform(delete(BASE_URL + "/" + POSTER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isConflict());
    }

    @Test
    void deletePoster_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + POSTER_ID))
                .andExpect(status().isUnauthorized());
    }
}
