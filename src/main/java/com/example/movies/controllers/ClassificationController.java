package com.example.movies.controllers;

import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.services.classification.ClassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/countries/{countryId}/classifications")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping
    public ResponseEntity<List<ClassificationResponse>> getByCountry(@PathVariable UUID countryId) {
        return ResponseEntity.ok(classificationService.findByCountry(countryId));
    }
}
