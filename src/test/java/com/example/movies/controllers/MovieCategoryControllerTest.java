package com.example.movies.controllers;

import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.dtos.movie.request.MovieCategoryRequest;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.MovieCategoryService;
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
@WebMvcTest(MovieCategoryController.class)
class MovieCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieCategoryService movieCategoryService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/movies/" + MOVIE_ID + "/categories";

    private CategoryResponse buildCategoryResponse() {
        return new CategoryResponse(CATEGORY_ID, "Action", true);
    }

    // ── GET /v1/movies/{movieId}/categories ────────────────────────────────

    @Test
    void getCategories_withAdminRole_returnsOk() throws Exception {
        when(movieCategoryService.getCategories(MOVIE_ID)).thenReturn(List.of(buildCategoryResponse()));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Action"));
    }

    @Test
    void getCategories_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCategories_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCategories_whenMovieNotFound_returnsNotFound() throws Exception {
        when(movieCategoryService.getCategories(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies/{movieId}/categories ───────────────────────────────

    @Test
    void addCategory_withAdminRole_returnsCreated() throws Exception {
        MovieCategoryRequest request = new MovieCategoryRequest(CATEGORY_ID);
        when(movieCategoryService.addCategory(MOVIE_ID, CATEGORY_ID)).thenReturn(List.of(buildCategoryResponse()));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("Action"));
    }

    @Test
    void addCategory_withNullCategoryId_returnsBadRequest() throws Exception {
        MovieCategoryRequest request = new MovieCategoryRequest(null);

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCategory_whenAlreadyAssigned_returnsConflict() throws Exception {
        MovieCategoryRequest request = new MovieCategoryRequest(CATEGORY_ID);
        when(movieCategoryService.addCategory(MOVIE_ID, CATEGORY_ID))
                .thenThrow(new ConflictException("Category already assigned"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── DELETE /v1/movies/{movieId}/categories/{categoryId} ────────────────

    @Test
    void removeCategory_withAdminRole_returnsOk() throws Exception {
        when(movieCategoryService.removeCategory(MOVIE_ID, CATEGORY_ID)).thenReturn(List.of());

        mockMvc.perform(delete(BASE_URL + "/" + CATEGORY_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void removeCategory_whenNotFound_returnsNotFound() throws Exception {
        when(movieCategoryService.removeCategory(MOVIE_ID, CATEGORY_ID))
                .thenThrow(new ResourceNotFoundException("Category not assigned to movie"));

        mockMvc.perform(delete(BASE_URL + "/" + CATEGORY_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeCategory_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + CATEGORY_ID))
                .andExpect(status().isUnauthorized());
    }
}
