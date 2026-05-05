package com.example.movies.services.classification;

import com.example.movies.dtos.classification.response.ClassificationResponse;
import com.example.movies.repositories.classification.ClassificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClassificationService {

    private final ClassificationRepository classificationRepository;

    public ClassificationService(ClassificationRepository classificationRepository) {
        this.classificationRepository = classificationRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassificationResponse> findAll() {
        return classificationRepository.findAll()
                .stream()
                .map(ClassificationResponse::from)
                .toList();
    }
}
