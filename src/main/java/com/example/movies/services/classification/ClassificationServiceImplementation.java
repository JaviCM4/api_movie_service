package com.example.movies.services.classification;

import com.example.movies.dtos.classification.request.CreateClassificationRequest;
import com.example.movies.dtos.classification.request.UpdateClassificationRequest;
import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.classification.Classification;
import com.example.movies.models.country.Country;
import com.example.movies.repositories.classification.ClassificationRepository;
import com.example.movies.repositories.country.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClassificationServiceImplementation implements ClassificationService {

    private final ClassificationRepository classificationRepository;
    private final CountryRepository countryRepository;

    @Autowired
    public ClassificationServiceImplementation(ClassificationRepository classificationRepository, CountryRepository countryRepository) {
        this.classificationRepository = classificationRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassificationResponse> findByCountry(UUID countryId) throws ResourceNotFoundException {
        if (!countryRepository.existsById(countryId)) {
            throw new ResourceNotFoundException("País no encontrado con id: " + countryId);
        }
        return classificationRepository.findByCountryIdAndIsActiveTrue(countryId)
                .stream()
                .map(ClassificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassificationResponse> findAllByCountry(UUID countryId) throws ResourceNotFoundException {
        if (!countryRepository.existsById(countryId)) {
            throw new ResourceNotFoundException("País no encontrado con id: " + countryId);
        }
        return classificationRepository.findByCountryId(countryId)
                .stream()
                .map(ClassificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassificationResponse createClassification(UUID countryId, CreateClassificationRequest dto)
            throws ResourceNotFoundException {
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("País no encontrado con id: " + countryId));
        Classification classification = new Classification();
        classification.setCountry(country);
        classification.setName(dto.getName());
        classification.setAgeLimit(dto.getAgeLimit());
        return ClassificationResponse.from(classificationRepository.save(classification));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassificationResponse updateClassification(UUID classificationId, UpdateClassificationRequest dto)
            throws ResourceNotFoundException {
        Classification classification = classificationRepository.findById(classificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con id: " + classificationId));
        if (dto.getName() != null) classification.setName(dto.getName());
        if (dto.getAgeLimit() != null) classification.setAgeLimit(dto.getAgeLimit());
        return ClassificationResponse.from(classificationRepository.save(classification));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassificationResponse toggleClassification(UUID classificationId) throws ResourceNotFoundException {
        Classification classification = classificationRepository.findById(classificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Clasificación no encontrada con id: " + classificationId));
        classification.setActive(!classification.isActive());
        return ClassificationResponse.from(classificationRepository.save(classification));
    }
}
