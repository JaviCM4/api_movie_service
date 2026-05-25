package com.example.movies.controllers;

import com.example.movies.dtos.classification.request.CreateClassificationRequest;
import com.example.movies.dtos.classification.request.UpdateClassificationRequest;
import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.classification.ClassificationService;
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
@WebMvcTest(ClassificationController.class)
class ClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClassificationService classificationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID COUNTRY_ID = UUID.randomUUID();
    private static final UUID CLASSIFICATION_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/countries/" + COUNTRY_ID + "/classifications";

    private ClassificationResponse buildResponse(boolean active) {
        return new ClassificationResponse(CLASSIFICATION_ID, "PG-13", 13, "USA", active);
    }

    // ── GET /v1/countries/{countryId}/classifications ──────────────────────

    @Test
    void getByCountry_withAuth_returnsOk() throws Exception {
        when(classificationService.findByCountry(COUNTRY_ID)).thenReturn(List.of(buildResponse(true)));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("PG-13"))
                .andExpect(jsonPath("$[0].ageLimit").value(13));
    }

    @Test
    void getByCountry_withoutAuth_returnsOk() throws Exception {
        when(classificationService.findByCountry(COUNTRY_ID)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    void getByCountry_whenCountryNotFound_returnsNotFound() throws Exception {
        when(classificationService.findByCountry(COUNTRY_ID))
                .thenThrow(new ResourceNotFoundException("Country not found"));

        mockMvc.perform(get(BASE_URL)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ── GET /v1/countries/{countryId}/classifications/all ─────────────────

    @Test
    void getAllByCountry_withAdminRole_returnsOk() throws Exception {
        when(classificationService.findAllByCountry(COUNTRY_ID))
                .thenReturn(List.of(buildResponse(true), buildResponse(false)));

        mockMvc.perform(get(BASE_URL + "/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllByCountry_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllByCountry_withWrongRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ── POST /v1/countries/{countryId}/classifications ────────────────────

    @Test
    void createClassification_withAdminRole_returnsCreated() throws Exception {
        CreateClassificationRequest request = new CreateClassificationRequest("PG-13", 13);
        when(classificationService.createClassification(eq(COUNTRY_ID), any(CreateClassificationRequest.class)))
                .thenReturn(buildResponse(true));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("PG-13"));
    }

    @Test
    void createClassification_withBlankName_returnsBadRequest() throws Exception {
        CreateClassificationRequest request = new CreateClassificationRequest("", 13);

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createClassification_whenCountryNotFound_returnsNotFound() throws Exception {
        CreateClassificationRequest request = new CreateClassificationRequest("PG-13", 13);
        when(classificationService.createClassification(eq(COUNTRY_ID), any(CreateClassificationRequest.class)))
                .thenThrow(new ResourceNotFoundException("Country not found"));

        mockMvc.perform(post(BASE_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /v1/countries/{countryId}/classifications/{classificationId} ─

    @Test
    void updateClassification_withAdminRole_returnsOk() throws Exception {
        UpdateClassificationRequest request = new UpdateClassificationRequest("R", 17);
        ClassificationResponse response = new ClassificationResponse(CLASSIFICATION_ID, "R", 17, "USA", true);
        when(classificationService.updateClassification(eq(CLASSIFICATION_ID), any(UpdateClassificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + CLASSIFICATION_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("R"));
    }

    @Test
    void updateClassification_whenNotFound_returnsNotFound() throws Exception {
        UpdateClassificationRequest request = new UpdateClassificationRequest("R", 17);
        when(classificationService.updateClassification(eq(CLASSIFICATION_ID), any(UpdateClassificationRequest.class)))
                .thenThrow(new ResourceNotFoundException("Classification not found"));

        mockMvc.perform(patch(BASE_URL + "/" + CLASSIFICATION_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /v1/countries/{countryId}/classifications/{id}/toggle ────────

    @Test
    void toggleClassification_withAdminRole_returnsOk() throws Exception {
        ClassificationResponse response = buildResponse(false);
        when(classificationService.toggleClassification(CLASSIFICATION_ID)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/" + CLASSIFICATION_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleClassification_whenNotFound_returnsNotFound() throws Exception {
        when(classificationService.toggleClassification(CLASSIFICATION_ID))
                .thenThrow(new ResourceNotFoundException("Classification not found"));

        mockMvc.perform(patch(BASE_URL + "/" + CLASSIFICATION_ID + "/toggle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}
