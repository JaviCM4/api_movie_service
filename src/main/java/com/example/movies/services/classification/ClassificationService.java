package com.example.movies.services.classification;

import com.example.movies.dtos.classification.request.CreateClassificationRequest;
import com.example.movies.dtos.classification.request.UpdateClassificationRequest;
import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ClassificationService {

    List<ClassificationResponse> findByCountry(UUID countryId) throws ResourceNotFoundException;

    List<ClassificationResponse> findAllByCountry(UUID countryId) throws ResourceNotFoundException;

    ClassificationResponse createClassification(UUID countryId, CreateClassificationRequest dto) throws ResourceNotFoundException;

    ClassificationResponse updateClassification(UUID classificationId, UpdateClassificationRequest dto) throws ResourceNotFoundException;

    ClassificationResponse toggleClassification(UUID classificationId) throws ResourceNotFoundException;
}
