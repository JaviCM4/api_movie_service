package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateRatingRequest;
import com.example.movies.dtos.movie.request.UpdateRatingRequest;
import com.example.movies.dtos.movie.response.RatingSummaryResponse;
import com.example.movies.dtos.movie.response.UserMovieRatingResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.RatingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
@WebMvcTest(RatingController.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID RATING_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private RatingSummaryResponse buildSummaryResponse() {
        return new RatingSummaryResponse(List.of(), 4.0);
    }

    // ── GET /v1/movies/{movieId}/ratings ───────────────────────────────────

    @Test
    void getRatings_withAuth_returnsOk() throws Exception {
        when(ratingService.findRatingsByMovie(MOVIE_ID)).thenReturn(buildSummaryResponse());

        mockMvc.perform(get("/v1/movies/" + MOVIE_ID + "/ratings")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore").value(4.0));
    }

    @Test
    void getRatings_withoutAuth_returnsOk() throws Exception {
        when(ratingService.findRatingsByMovie(MOVIE_ID)).thenReturn(buildSummaryResponse());

        mockMvc.perform(get("/v1/movies/" + MOVIE_ID + "/ratings"))
                .andExpect(status().isOk());
    }

    @Test
    void getRatings_whenMovieNotFound_returnsNotFound() throws Exception {
        when(ratingService.findRatingsByMovie(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get("/v1/movies/" + MOVIE_ID + "/ratings")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies/{movieId}/ratings ──────────────────────────────────

    @Test
    void createRating_withClientRole_returnsCreated() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest((short) 4);
        when(ratingService.createRating(eq(MOVIE_ID), eq(USER_ID), any(CreateRatingRequest.class)))
                .thenReturn(buildSummaryResponse());

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/ratings")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.averageScore").value(4.0));
    }

    @Test
    void createRating_withScoreOutOfRange_returnsBadRequest() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest((short) 10);

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/ratings")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRating_whenAlreadyRated_returnsConflict() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest((short) 4);
        when(ratingService.createRating(any(), any(), any(CreateRatingRequest.class)))
                .thenThrow(new ConflictException("User already rated this movie"));

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/ratings")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createRating_withWrongRole_returnsForbidden() throws Exception {
        CreateRatingRequest request = new CreateRatingRequest((short) 4);

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/ratings")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /v1/ratings/{ratingId} ───────────────────────────────────────

    @Test
    void updateRating_withClientRole_returnsOk() throws Exception {
        UpdateRatingRequest request = new UpdateRatingRequest((short) 5);
        RatingSummaryResponse response = new RatingSummaryResponse(List.of(), 5.0);
        when(ratingService.updateRating(eq(RATING_ID), eq(USER_ID), any(UpdateRatingRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/v1/ratings/" + RATING_ID)
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore").value(5.0));
    }

    @Test
    void updateRating_whenNotFound_returnsNotFound() throws Exception {
        UpdateRatingRequest request = new UpdateRatingRequest((short) 5);
        when(ratingService.updateRating(any(), any(), any(UpdateRatingRequest.class)))
                .thenThrow(new ResourceNotFoundException("Rating not found"));

        mockMvc.perform(patch("/v1/ratings/" + RATING_ID)
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── GET /v1/ratings/user ───────────────────────────────────────────────

    @Test
    void getMyRatings_withClientRole_returnsOk() throws Exception {
        UserMovieRatingResponse response = new UserMovieRatingResponse(
                RATING_ID, (short) 4, LocalDateTime.now(), false,
                MOVIE_ID, "Forrest Gump", "http://example.com/poster.jpg");
        when(ratingService.findRatingsByUser(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/ratings/user")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(4));
    }

    @Test
    void getMyRatings_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/ratings/user"))
                .andExpect(status().isUnauthorized());
    }
}
