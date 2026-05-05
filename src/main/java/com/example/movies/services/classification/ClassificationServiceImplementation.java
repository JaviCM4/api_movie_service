package com.example.movies.services.classification;

import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.repositories.classification.ClassificationRepository;
import com.example.movies.repositories.country.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClassificationServiceImplementation implements ClassificationService {

    private final ClassificationRepository classificationRepository;
    private final CountryRepository countryRepository;

    public ClassificationServiceImplementation(ClassificationRepository classificationRepository,
                                               CountryRepository countryRepository) {
        this.classificationRepository = classificationRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassificationResponse> findByCountry(UUID countryId) throws ResourceNotFoundException {
        if (!countryRepository.existsById(countryId)) {
            throw new ResourceNotFoundException("Country not found with id: " + countryId);
        }
        return classificationRepository.findByCountryId(countryId)
                .stream()
                .map(ClassificationResponse::from)
                .toList();
    }
}
