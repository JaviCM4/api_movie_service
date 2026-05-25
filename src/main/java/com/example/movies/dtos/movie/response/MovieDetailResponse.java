package com.example.movies.dtos.movie.response;

import com.example.movies.models.movie.*;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
public class MovieDetailResponse {

    UUID id;
    String title;
    String synopsis;
    Integer duration;
    String trailerLink;
    String originalLanguage;
    LocalDate releaseDate;
    List<ClassificationDetailResponse> classifications;
    List<CastDetailResponse> cast;
    List<String> categories;
    List<PosterResponse> posters;
    List<PeopleRoleResponse> crew;

    public static MovieDetailResponse from(
            Movie movie,
            List<Cast> casts,
            List<MovieCountryInfo> movieCountryInfos,
            List<MovieCategory> movieCategories,
            List<Poster> posters,
            List<MoviePeople> moviePeoples) {

        return new MovieDetailResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getSynopsis(),
                movie.getDuration(),
                movie.getTrailerLink(),
                movie.getOriginalLanguage(),
                movie.getReleaseDate(),
                movieCountryInfos.stream().map(ClassificationDetailResponse::from).toList(),
                casts.stream().map(CastDetailResponse::from).toList(),
                movieCategories.stream().map(mc -> mc.getCategory().getName()).toList(),
                posters.stream().map(PosterResponse::from).toList(),
                moviePeoples.stream().map(PeopleRoleResponse::from).toList()
        );
    }
}
