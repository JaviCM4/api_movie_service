package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieCountryInfo;
import com.example.movies.models.movie.Poster;
import lombok.Value;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
public class MovieBriefResponse {

    UUID id;
    String title;
    String poster;
    List<String> classifications;

    public static MovieBriefResponse from(Movie movie, List<Poster> posters, List<MovieCountryInfo> countryInfos) {
        String mainPoster = posters.stream()
                .filter(Poster::isMain)
                .map(Poster::getUrlImage)
                .findFirst()
                .orElse(null);

        List<String> classifs = countryInfos.stream()
                .map(mci -> mci.getClassification().getName())
                .distinct()
                .collect(Collectors.toList());

        return new MovieBriefResponse(movie.getId(), movie.getTitle(), mainPoster, classifs);
    }
}
