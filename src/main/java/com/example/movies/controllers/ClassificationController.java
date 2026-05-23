package com.example.movies.controllers;

import com.example.movies.dtos.classification.request.CreateClassificationRequest;
import com.example.movies.dtos.classification.request.UpdateClassificationRequest;
import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.classification.ClassificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/countries/{countryId}/classifications")
public class ClassificationController {

    private final ClassificationService classificationService;

    @Autowired
    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping
    public ResponseEntity<List<ClassificationResponse>> getByCountry(@PathVariable UUID countryId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(classificationService.findByCountry(countryId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ClassificationResponse>> getAllByCountry(@PathVariable UUID countryId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(classificationService.findAllByCountry(countryId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ClassificationResponse> createClassification(
            @PathVariable UUID countryId,
            @Valid @RequestBody CreateClassificationRequest request) throws ResourceNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classificationService.createClassification(countryId, request));
    }

    @PatchMapping("/{classificationId}")
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ClassificationResponse> updateClassification(
            @PathVariable UUID countryId,
            @PathVariable UUID classificationId,
            @Valid @RequestBody UpdateClassificationRequest request) throws ResourceNotFoundException {
        return ResponseEntity.ok(classificationService.updateClassification(classificationId, request));
    }

    @PatchMapping("/{classificationId}/toggle")
    @PreAuthorize("hasAnyRole('CINEMA_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ClassificationResponse> toggleClassification(
            @PathVariable UUID countryId,
            @PathVariable UUID classificationId) throws ResourceNotFoundException {
        return ResponseEntity.ok(classificationService.toggleClassification(classificationId));
    }
}
