package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieCountryInfo;
import com.example.movies.models.movie.Poster;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
public class MovieSummaryResponse {

    UUID id;
    String title;
    Integer duration;
    String poster;
    LocalDate releaseDate;
    List<ClassificationDetailResponse> classifications;

    public static MovieSummaryResponse from(Movie movie, List<MovieCountryInfo> countryInfos, List<Poster> posters) {
        String mainPoster = posters.stream()
                .filter(Poster::isMain)
                .map(Poster::getUrlImage)
                .findFirst()
                .orElse(null);

        return new MovieSummaryResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDuration(),
                mainPoster,
                movie.getReleaseDate(),
                countryInfos.stream().map(ClassificationDetailResponse::from).toList()
        );
    }
}
