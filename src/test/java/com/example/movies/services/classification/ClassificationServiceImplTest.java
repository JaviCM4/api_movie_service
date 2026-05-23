package com.example.movies.services.classification;

import com.example.movies.dtos.classification.request.CreateClassificationRequest;
import com.example.movies.dtos.classification.request.UpdateClassificationRequest;
import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.classification.Classification;
import com.example.movies.models.country.Country;
import com.example.movies.repositories.classification.ClassificationRepository;
import com.example.movies.repositories.country.CountryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassificationServiceImplTest {

    private static final UUID COUNTRY_ID          = UUID.randomUUID();
    private static final UUID CLASSIFICATION_ID   = UUID.randomUUID();
    private static final String COUNTRY_NAME      = "United States";

    @Mock private ClassificationRepository classificationRepository;
    @Mock private CountryRepository        countryRepository;

    @InjectMocks
    private ClassificationServiceImplementation classificationService;

    // ── findByCountry (solo activos) ──────────────────────────────────────

    @Test
    void testFindByCountryReturnsOnlyActive() throws Exception {
        // Arrange
        Country country = buildCountry(COUNTRY_ID, COUNTRY_NAME);
        Classification active   = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, true);
        Classification inactive = buildClassification(UUID.randomUUID(), "R",     17, country, false);

        when(countryRepository.existsById(COUNTRY_ID)).thenReturn(true);
        when(classificationRepository.findByCountryIdAndIsActiveTrue(COUNTRY_ID))
                .thenReturn(List.of(active));

        // Act
        List<ClassificationResponse> result = classificationService.findByCountry(COUNTRY_ID);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("PG-13", result.get(0).getName()),
                () -> assertTrue(result.get(0).isActive())
        );
        verify(classificationRepository, never()).findByCountryId(any());
    }

    @Test
    void testFindByCountryWhenCountryNotFound() {
        // Arrange
        when(countryRepository.existsById(COUNTRY_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> classificationService.findByCountry(COUNTRY_ID));
        verify(classificationRepository, never()).findByCountryIdAndIsActiveTrue(any());
    }

    // ── findAllByCountry (activos + inactivos) ────────────────────────────

    @Test
    void testFindAllByCountryReturnsAll() throws Exception {
        // Arrange
        Country country = buildCountry(COUNTRY_ID, COUNTRY_NAME);
        Classification active   = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, true);
        Classification inactive = buildClassification(UUID.randomUUID(), "R",     17, country, false);

        when(countryRepository.existsById(COUNTRY_ID)).thenReturn(true);
        when(classificationRepository.findByCountryId(COUNTRY_ID))
                .thenReturn(List.of(active, inactive));

        // Act
        List<ClassificationResponse> result = classificationService.findAllByCountry(COUNTRY_ID);

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.get(0).isActive()),
                () -> assertFalse(result.get(1).isActive())
        );
    }

    @Test
    void testFindAllByCountryWhenCountryNotFound() {
        // Arrange
        when(countryRepository.existsById(COUNTRY_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> classificationService.findAllByCountry(COUNTRY_ID));
        verify(classificationRepository, never()).findByCountryId(any());
    }

    // ── createClassification ──────────────────────────────────────────────

    @Test
    void testCreateClassification() throws Exception {
        // Arrange
        ArgumentCaptor<Classification> captor = ArgumentCaptor.forClass(Classification.class);
        CreateClassificationRequest request   = new CreateClassificationRequest("PG-13", 13);

        Country country = buildCountry(COUNTRY_ID, COUNTRY_NAME);
        Classification saved = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, true);

        when(countryRepository.findById(COUNTRY_ID)).thenReturn(Optional.of(country));
        when(classificationRepository.save(any(Classification.class))).thenReturn(saved);

        // Act
        ClassificationResponse result = classificationService.createClassification(COUNTRY_ID, request);

        // Assert
        assertAll(
                () -> verify(classificationRepository).save(captor.capture()),
                () -> assertEquals("PG-13", captor.getValue().getName()),
                () -> assertEquals(13, captor.getValue().getAgeLimit()),
                () -> assertEquals(COUNTRY_NAME, result.getCountry()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testCreateClassificationWhenCountryNotFound() {
        // Arrange
        CreateClassificationRequest request = new CreateClassificationRequest("PG-13", 13);
        when(countryRepository.findById(COUNTRY_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> classificationService.createClassification(COUNTRY_ID, request));
        verify(classificationRepository, never()).save(any());
    }

    // ── updateClassification ──────────────────────────────────────────────

    @Test
    void testUpdateClassification() throws Exception {
        // Arrange
        ArgumentCaptor<Classification> captor = ArgumentCaptor.forClass(Classification.class);
        UpdateClassificationRequest request   = new UpdateClassificationRequest("R", 17);

        Country country   = buildCountry(COUNTRY_ID, COUNTRY_NAME);
        Classification existing = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, true);
        Classification saved    = buildClassification(CLASSIFICATION_ID, "R",     17, country, true);

        when(classificationRepository.findById(CLASSIFICATION_ID)).thenReturn(Optional.of(existing));
        when(classificationRepository.save(any(Classification.class))).thenReturn(saved);

        // Act
        ClassificationResponse result = classificationService.updateClassification(CLASSIFICATION_ID, request);

        // Assert
        assertAll(
                () -> verify(classificationRepository).save(captor.capture()),
                () -> assertEquals("R",  captor.getValue().getName()),
                () -> assertEquals(17,   captor.getValue().getAgeLimit()),
                () -> assertEquals("R",  result.getName()),
                () -> assertEquals(17,   result.getAgeLimit())
        );
    }

    @Test
    void testUpdateClassificationWhenNotFound() {
        // Arrange
        UpdateClassificationRequest request = new UpdateClassificationRequest("R", 17);
        when(classificationRepository.findById(CLASSIFICATION_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> classificationService.updateClassification(CLASSIFICATION_ID, request));
        verify(classificationRepository, never()).save(any());
    }

    // ── toggleClassification ──────────────────────────────────────────────

    @Test
    void testToggleClassificationDeactivates() throws Exception {
        // Arrange
        Country country   = buildCountry(COUNTRY_ID, COUNTRY_NAME);
        Classification active = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, true);
        Classification saved  = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, false);

        when(classificationRepository.findById(CLASSIFICATION_ID)).thenReturn(Optional.of(active));
        when(classificationRepository.save(any(Classification.class))).thenReturn(saved);

        // Act
        ClassificationResponse result = classificationService.toggleClassification(CLASSIFICATION_ID);

        // Assert
        assertAll(
                () -> verify(classificationRepository).save(active),
                () -> assertFalse(result.isActive())
        );
    }

    @Test
    void testToggleClassificationActivates() throws Exception {
        // Arrange
        Country country     = buildCountry(COUNTRY_ID, COUNTRY_NAME);
        Classification inactive = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, false);
        Classification saved    = buildClassification(CLASSIFICATION_ID, "PG-13", 13, country, true);

        when(classificationRepository.findById(CLASSIFICATION_ID)).thenReturn(Optional.of(inactive));
        when(classificationRepository.save(any(Classification.class))).thenReturn(saved);

        // Act
        ClassificationResponse result = classificationService.toggleClassification(CLASSIFICATION_ID);

        // Assert
        assertAll(
                () -> verify(classificationRepository).save(inactive),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testToggleClassificationWhenNotFound() {
        // Arrange
        when(classificationRepository.findById(CLASSIFICATION_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> classificationService.toggleClassification(CLASSIFICATION_ID));
        verify(classificationRepository, never()).save(any());
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private Country buildCountry(UUID id, String name) {
        Country c = new Country();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private Classification buildClassification(UUID id, String name, int ageLimit,
                                               Country country, boolean active) {
        Classification c = new Classification();
        c.setId(id);
        c.setName(name);
        c.setAgeLimit(ageLimit);
        c.setCountry(country);
        c.setActive(active);
        return c;
    }
}
