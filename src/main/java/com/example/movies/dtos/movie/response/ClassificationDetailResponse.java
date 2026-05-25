package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.MovieCountryInfo;
import lombok.Value;

@Value
public class ClassificationDetailResponse {

    String name;
    Integer ageLimit;
    String country;

    public static ClassificationDetailResponse from(MovieCountryInfo mci) {
        return new ClassificationDetailResponse(
                mci.getClassification().getName(),
                mci.getClassification().getAgeLimit(),
                mci.getClassification().getCountry().getName()
        );
    }
}
