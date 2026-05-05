package com.example.movies.controllers;

import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.services.classification.ClassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classifications")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping
    public ResponseEntity<List<ClassificationResponse>> getAll() {
        return ResponseEntity.ok(classificationService.findAll());
    }
}
