package com.example.movies.controllers;

import com.example.movies.dtos.movie.response.MovieCountryInfoResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.MovieCountryInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.movies.security.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com")
@Import(SecurityConfig.class)
@WebMvcTest(MovieCountryInfoController.class)
class MovieCountryInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieCountryInfoService movieCountryInfoService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID CLASSIFICATION_ID = UUID.randomUUID();
    private static final UUID COUNTRY_ID = UUID.randomUUID();
    private static final UUID MOVIE_COUNTRY_INFO_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/movies/" + MOVIE_ID + "/country-info";

    private MovieCountryInfoResponse buildResponse(boolean active) {
        return new MovieCountryInfoResponse(
                MOVIE_COUNTRY_INFO_ID, CLASSIFICATION_ID, "PG-13", 13, COUNTRY_ID, "USA", active);
    }

    // ── GET /v1/movies/{movieId}/country-info ──────────────────────────────

    @Test
    void getCountryInfo_withAdminRole_returnsOk() throws Exception {
        when(movieCountryInfoService.getCountryInfo(MOVIE_ID)).thenReturn(List.of(buildResponse(true)));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].classificationName").value("PG-13"))
                .andExpect(jsonPath("$[0].countryName").value("USA"));
    }

    @Test
    void getCountryInfo_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCountryInfo_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCountryInfo_whenMovieNotFound_returnsNotFound() throws Exception {
        when(movieCountryInfoService.getCountryInfo(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies/{movieId}/country-info/{classificationId} ──────────

    @Test
    void addClassification_withAdminRole_returnsCreated() throws Exception {
        when(movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID))
                .thenReturn(List.of(buildResponse(true)));

        mockMvc.perform(post(BASE_URL + "/" + CLASSIFICATION_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].classificationName").value("PG-13"));
    }

    @Test
    void addClassification_whenAlreadyExists_returnsConflict() throws Exception {
        when(movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID))
                .thenThrow(new ConflictException("Classification already assigned"));

        mockMvc.perform(post(BASE_URL + "/" + CLASSIFICATION_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isConflict());
    }

    @Test
    void addClassification_whenMovieNotFound_returnsNotFound() throws Exception {
        when(movieCountryInfoService.addClassification(MOVIE_ID, CLASSIFICATION_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(post(BASE_URL + "/" + CLASSIFICATION_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /v1/movies/{movieId}/country-info/{movieCountryInfoId}/toggle ─

    @Test
    void toggleActive_withAdminRole_returnsOk() throws Exception {
        MovieCountryInfoResponse response = buildResponse(false);
        when(movieCountryInfoService.toggleActive(MOVIE_COUNTRY_INFO_ID)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + MOVIE_COUNTRY_INFO_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleActive_whenNotFound_returnsNotFound() throws Exception {
        when(movieCountryInfoService.toggleActive(MOVIE_COUNTRY_INFO_ID))
                .thenThrow(new ResourceNotFoundException("MovieCountryInfo not found"));

        mockMvc.perform(patch(BASE_URL + "/" + MOVIE_COUNTRY_INFO_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /v1/movies/{movieId}/country-info/{movieCountryInfoId} ───────

    @Test
    void removeClassification_withAdminRole_returnsOk() throws Exception {
        when(movieCountryInfoService.removeClassification(MOVIE_ID, MOVIE_COUNTRY_INFO_ID))
                .thenReturn(List.of());

        mockMvc.perform(delete(BASE_URL + "/" + MOVIE_COUNTRY_INFO_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void removeClassification_whenNotFound_returnsNotFound() throws Exception {
        when(movieCountryInfoService.removeClassification(MOVIE_ID, MOVIE_COUNTRY_INFO_ID))
                .thenThrow(new ResourceNotFoundException("MovieCountryInfo not found"));

        mockMvc.perform(delete(BASE_URL + "/" + MOVIE_COUNTRY_INFO_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeClassification_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + MOVIE_COUNTRY_INFO_ID))
                .andExpect(status().isUnauthorized());
    }
}
