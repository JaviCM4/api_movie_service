package com.example.movies.dtos.classification.response;

import com.example.movies.models.classification.Classification;
import lombok.Value;

import java.util.UUID;

@Value
public class ClassificationResponse {

    UUID id;
    String name;
    Integer ageLimit;
    String country;

    public static ClassificationResponse from(Classification classification) {
        return new ClassificationResponse(
                classification.getId(),
                classification.getName(),
                classification.getAgeLimit(),
                classification.getCountry().getName()
        );
    }
}
