package com.example.movies.services.classification;

import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ClassificationService {

    List<ClassificationResponse> findByCountry(UUID countryId) throws ResourceNotFoundException;
}
