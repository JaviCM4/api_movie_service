package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.MovieCountryInfo;
import lombok.Value;

import java.util.UUID;

@Value
public class MovieCountryInfoResponse {

    UUID movieCountryInfoId;
    UUID classificationId;
    String classificationName;
    Integer ageLimit;
    UUID countryId;
    String countryName;
    boolean isActive;

    public static MovieCountryInfoResponse from(MovieCountryInfo mci) {
        return new MovieCountryInfoResponse(
                mci.getId(),
                mci.getClassification().getId(),
                mci.getClassification().getName(),
                mci.getClassification().getAgeLimit(),
                mci.getClassification().getCountry().getId(),
                mci.getClassification().getCountry().getName(),
                mci.isActive()
        );
    }
}
