package com.example.movies.controllers;

import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.classification.ClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
}
