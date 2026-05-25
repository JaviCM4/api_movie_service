package com.example.movies.controllers;

import com.example.movies.dtos.category.request.CreateCategoryRequest;
import com.example.movies.dtos.category.request.UpdateCategoryRequest;
import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.category.CategoryService;
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
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/categories";

    private CategoryResponse buildCategoryResponse(boolean active) {
        return new CategoryResponse(CATEGORY_ID, "Action", active);
    }

    // ── GET /v1/categories (public) ────────────────────────────────────────

    @Test
    void getActiveCategories_withoutAuth_returnsOk() throws Exception {
        when(categoryService.findAllActive()).thenReturn(List.of(buildCategoryResponse(true)));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Action"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getActiveCategories_returnsEmptyList() throws Exception {
        when(categoryService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /v1/categories/all ─────────────────────────────────────────────

    @Test
    void getAllCategories_withAdminRole_returnsOk() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(
                buildCategoryResponse(true),
                buildCategoryResponse(false)));

        mockMvc.perform(get(BASE_URL + "/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllCategories_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllCategories_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ── POST /v1/categories ────────────────────────────────────────────────

    @Test
    void createCategory_withAdminRole_returnsCreated() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Action");
        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenReturn(buildCategoryResponse(true));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Action"));
    }

    @Test
    void createCategory_withBlankName_returnsBadRequest() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("");

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_whenDuplicated_returnsConflict() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Action");
        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenThrow(new ConflictException("Category already exists"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/categories/{id} ──────────────────────────────────────────

    @Test
    void updateCategory_withAdminRole_returnsOk() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Drama");
        CategoryResponse response = new CategoryResponse(CATEGORY_ID, "Drama", true);
        when(categoryService.updateCategory(eq(CATEGORY_ID), any(UpdateCategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + CATEGORY_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drama"));
    }

    @Test
    void updateCategory_whenNotFound_returnsNotFound() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Drama");
        when(categoryService.updateCategory(eq(CATEGORY_ID), any(UpdateCategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(patch(BASE_URL + "/" + CATEGORY_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /v1/categories/{id}/toggle ──────────────────────────────────

    @Test
    void toggleActive_withAdminRole_returnsOk() throws Exception {
        CategoryResponse response = buildCategoryResponse(false);
        when(categoryService.toggleActive(CATEGORY_ID)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + CATEGORY_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleActive_whenNotFound_returnsNotFound() throws Exception {
        when(categoryService.toggleActive(CATEGORY_ID))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(patch(BASE_URL + "/" + CATEGORY_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}
