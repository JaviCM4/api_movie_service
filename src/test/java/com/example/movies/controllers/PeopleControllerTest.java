package com.example.movies.controllers;

import com.example.movies.dtos.people.request.CreatePeopleRequest;
import com.example.movies.dtos.people.request.UpdatePeopleRequest;
import com.example.movies.dtos.people.response.PeopleResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.people.PeopleService;
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
@WebMvcTest(PeopleController.class)
class PeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PeopleService peopleService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID PEOPLE_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/people";

    private PeopleResponse buildPeopleResponse(boolean active) {
        return new PeopleResponse(PEOPLE_ID, "Steven Spielberg", active);
    }

    // ── GET /v1/people ─────────────────────────────────────────────────────

    @Test
    void getAll_withAdminRole_returnsOk() throws Exception {
        when(peopleService.findAll()).thenReturn(List.of(buildPeopleResponse(true)));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Steven Spielberg"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getAll_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ── POST /v1/people ────────────────────────────────────────────────────

    @Test
    void createPeople_withAdminRole_returnsCreated() throws Exception {
        CreatePeopleRequest request = new CreatePeopleRequest("Steven Spielberg");
        when(peopleService.createPeople(any(CreatePeopleRequest.class))).thenReturn(buildPeopleResponse(true));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Steven Spielberg"));
    }

    @Test
    void createPeople_withBlankName_returnsBadRequest() throws Exception {
        CreatePeopleRequest request = new CreatePeopleRequest("");

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPeople_whenDuplicated_returnsConflict() throws Exception {
        CreatePeopleRequest request = new CreatePeopleRequest("Steven Spielberg");
        when(peopleService.createPeople(any(CreatePeopleRequest.class)))
                .thenThrow(new ConflictException("Person already exists"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/people/{id} ──────────────────────────────────────────────

    @Test
    void updatePeople_withAdminRole_returnsOk() throws Exception {
        UpdatePeopleRequest request = new UpdatePeopleRequest("James Cameron");
        PeopleResponse response = new PeopleResponse(PEOPLE_ID, "James Cameron", true);
        when(peopleService.updatePeople(eq(PEOPLE_ID), any(UpdatePeopleRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + PEOPLE_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("James Cameron"));
    }

    @Test
    void updatePeople_whenNotFound_returnsNotFound() throws Exception {
        UpdatePeopleRequest request = new UpdatePeopleRequest("James Cameron");
        when(peopleService.updatePeople(eq(PEOPLE_ID), any(UpdatePeopleRequest.class)))
                .thenThrow(new ResourceNotFoundException("Person not found"));

        mockMvc.perform(patch(BASE_URL + "/" + PEOPLE_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePeople_whenDuplicated_returnsConflict() throws Exception {
        UpdatePeopleRequest request = new UpdatePeopleRequest("James Cameron");
        when(peopleService.updatePeople(eq(PEOPLE_ID), any(UpdatePeopleRequest.class)))
                .thenThrow(new ConflictException("Name already in use"));

        mockMvc.perform(patch(BASE_URL + "/" + PEOPLE_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /v1/people/{id}/toggle ───────────────────────────────────────

    @Test
    void togglePeople_withAdminRole_returnsOk() throws Exception {
        PeopleResponse response = buildPeopleResponse(false);
        when(peopleService.togglePeople(PEOPLE_ID)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + PEOPLE_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void togglePeople_whenNotFound_returnsNotFound() throws Exception {
        when(peopleService.togglePeople(PEOPLE_ID))
                .thenThrow(new ResourceNotFoundException("Person not found"));

        mockMvc.perform(patch(BASE_URL + "/" + PEOPLE_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}
