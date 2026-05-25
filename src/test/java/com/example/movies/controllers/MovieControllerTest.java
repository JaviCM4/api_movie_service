package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateMovieRequest;
import com.example.movies.dtos.movie.request.UpdateMovieRequest;
import com.example.movies.dtos.movie.response.MovieAdminResponse;
import com.example.movies.dtos.movie.response.MovieBriefResponse;
import com.example.movies.dtos.movie.response.MovieDetailResponse;
import com.example.movies.dtos.movie.response.MovieSummaryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.movies.security.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com")
@Import(SecurityConfig.class)
@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID COUNTRY_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/movies";

    private MovieSummaryResponse buildSummaryResponse() {
        return new MovieSummaryResponse(MOVIE_ID, "Forrest Gump", 142,
                "http://example.com/poster.jpg", LocalDate.of(1994, 7, 6), List.of());
    }

    private MovieDetailResponse buildDetailResponse() {
        return new MovieDetailResponse(MOVIE_ID, "Forrest Gump", "A great movie", 142,
                "http://example.com/trailer", "English", LocalDate.of(1994, 7, 6),
                List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private MovieAdminResponse buildAdminResponse() {
        return new MovieAdminResponse(MOVIE_ID, "Forrest Gump", "A great movie", 142,
                "http://example.com/trailer", "English", LocalDate.of(1994, 7, 6),
                true, true, LocalDateTime.now(), null);
    }

    // ── GET /v1/movies ─────────────────────────────────────────────────────

    @Test
    void getMovies_withoutAuth_returnsOk() throws Exception {
        when(movieService.findAllMoviesByCountry(eq(COUNTRY_ID), any(), any(), any(), any()))
                .thenReturn(List.of(buildSummaryResponse()));

        mockMvc.perform(get(BASE_URL).param("countryId", COUNTRY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Forrest Gump"));
    }

    @Test
    void getMovies_withFilters_returnsOk() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID classificationId = UUID.randomUUID();
        when(movieService.findAllMoviesByCountry(any(), any(), any(), any(), any()))
                .thenReturn(List.of(buildSummaryResponse()));

        mockMvc.perform(get(BASE_URL)
                        .param("countryId", COUNTRY_ID.toString())
                        .param("title", "Forrest")
                        .param("categoryId", categoryId.toString())
                        .param("classificationId", classificationId.toString())
                        .param("sort", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /v1/movies/{movieId} ───────────────────────────────────────────

    @Test
    void getMovieDetail_withoutAuth_returnsOk() throws Exception {
        when(movieService.findMovieById(MOVIE_ID, COUNTRY_ID)).thenReturn(buildDetailResponse());

        mockMvc.perform(get(BASE_URL + "/" + MOVIE_ID)
                        .param("countryId", COUNTRY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Forrest Gump"))
                .andExpect(jsonPath("$.synopsis").value("A great movie"));
    }

    @Test
    void getMovieDetail_whenNotFound_returnsNotFound() throws Exception {
        when(movieService.findMovieById(MOVIE_ID, COUNTRY_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get(BASE_URL + "/" + MOVIE_ID)
                        .param("countryId", COUNTRY_ID.toString()))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies ────────────────────────────────────────────────────

    @Test
    void createMovie_withAdminRole_returnsCreated() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest(
                List.of(UUID.randomUUID()),
                "Forrest Gump",
                "A great movie",
                142,
                "http://example.com/trailer",
                "English",
                LocalDate.now().plusYears(1),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        doNothing().when(movieService).createMovie(any(CreateMovieRequest.class));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createMovie_withBlankTitle_returnsBadRequest() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest(
                List.of(UUID.randomUUID()),
                "",
                "A great movie",
                142,
                null,
                "English",
                LocalDate.now().plusYears(1),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMovie_whenConflict_returnsConflict() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest(
                List.of(UUID.randomUUID()),
                "Forrest Gump",
                "A great movie",
                142,
                null,
                "English",
                LocalDate.now().plusYears(1),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        doThrow(new ConflictException("Movie already exists")).when(movieService).createMovie(any(CreateMovieRequest.class));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createMovie_withoutAuth_returnsUnauthorized() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest(
                List.of(UUID.randomUUID()),
                "Forrest Gump",
                "A great movie",
                142,
                null,
                "English",
                LocalDate.now().plusYears(1),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /v1/movies/{movieId}/admin ─────────────────────────────────────

    @Test
    void getMovieAdmin_withAdminRole_returnsOk() throws Exception {
        when(movieService.findMovieAdminById(MOVIE_ID)).thenReturn(buildAdminResponse());

        mockMvc.perform(get(BASE_URL + "/" + MOVIE_ID + "/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Forrest Gump"));
    }

    @Test
    void getMovieAdmin_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + MOVIE_ID + "/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMovieAdmin_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + MOVIE_ID + "/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMovieAdmin_whenNotFound_returnsNotFound() throws Exception {
        when(movieService.findMovieAdminById(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get(BASE_URL + "/" + MOVIE_ID + "/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /v1/movies/{movieId} ─────────────────────────────────────────

    @Test
    void updateMovie_withAdminRole_returnsNoContent() throws Exception {
        UpdateMovieRequest request = new UpdateMovieRequest("Updated Title", null, null, null, null, null, null, null);
        doNothing().when(movieService).updateMovie(eq(MOVIE_ID), any(UpdateMovieRequest.class));

        mockMvc.perform(patch(BASE_URL + "/" + MOVIE_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateMovie_whenNotFound_returnsNotFound() throws Exception {
        UpdateMovieRequest request = new UpdateMovieRequest("Updated Title", null, null, null, null, null, null, null);
        doThrow(new ResourceNotFoundException("Movie not found"))
                .when(movieService).updateMovie(eq(MOVIE_ID), any(UpdateMovieRequest.class));

        mockMvc.perform(patch(BASE_URL + "/" + MOVIE_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── GET /v1/movies/brief ───────────────────────────────────────────────

    @Test
    void getMoviesBrief_withoutAuth_returnsOk() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        MovieBriefResponse brief1 = new MovieBriefResponse(id1, "Forrest Gump", "http://example.com/p1.jpg", List.of("PG-13"));
        MovieBriefResponse brief2 = new MovieBriefResponse(id2, "Cast Away", "http://example.com/p2.jpg", List.of("PG"));
        when(movieService.findMoviesBrief(any())).thenReturn(List.of(brief1, brief2));

        mockMvc.perform(get(BASE_URL + "/brief")
                        .param("ids", id1.toString(), id2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Forrest Gump"));
    }
}
