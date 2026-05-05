package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.*;
import com.example.movies.dtos.movie.response.MovieDetailResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.actor.Actor;
import com.example.movies.models.category.Category;
import com.example.movies.models.classification.Classification;
import com.example.movies.models.movie.*;
import com.example.movies.models.people.People;
import com.example.movies.repositories.actor.ActorRepository;
import com.example.movies.repositories.category.CategoryRepository;
import com.example.movies.repositories.classification.ClassificationRepository;
import com.example.movies.repositories.movie.*;
import com.example.movies.repositories.people.PeopleRepository;
import com.example.movies.services.utils.ResolverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MovieServiceImplementation implements MovieService {

    private final MovieRepository movieRepository;
    private final ClassificationRepository classificationRepository;
    private final MovieCountryInfoRepository movieCountryInfoRepository;
    private final ActorRepository actorRepository;
    private final CategoryRepository categoryRepository;
    private final PeopleRepository peopleRepository;
    private final CastRepository castRepository;
    private final MovieCategoryRepository movieCategoryRepository;
    private final PosterRepository posterRepository;
    private final MoviePeopleRepository moviePeopleRepository;
    private final ResolverService resolverService;

    public MovieServiceImplementation(
            MovieRepository movieRepository,
            ClassificationRepository classificationRepository,
            MovieCountryInfoRepository movieCountryInfoRepository,
            ActorRepository actorRepository,
            CategoryRepository categoryRepository,
            PeopleRepository peopleRepository,
            CastRepository castRepository,
            MovieCategoryRepository movieCategoryRepository,
            PosterRepository posterRepository,
            MoviePeopleRepository moviePeopleRepository,
            ResolverService resolverService) {

        this.movieRepository = movieRepository;
        this.classificationRepository = classificationRepository;
        this.movieCountryInfoRepository = movieCountryInfoRepository;
        this.actorRepository = actorRepository;
        this.categoryRepository = categoryRepository;
        this.peopleRepository = peopleRepository;
        this.castRepository = castRepository;
        this.movieCategoryRepository = movieCategoryRepository;
        this.posterRepository = posterRepository;
        this.moviePeopleRepository = moviePeopleRepository;
        this.resolverService = resolverService;
    }

    @Override
    @Transactional
    public void createMovie(CreateMovieRequest dto) throws ResourceNotFoundException, ConflictException {
        List<AssignActorRequest> actorRequests  = resolverService.deduplicateByKey(dto.getActors(),  AssignActorRequest::getActorId);
        List<AssignPeopleRequest> peopleRequests = resolverService.deduplicateByKey(dto.getPeople(),  AssignPeopleRequest::getPeopleId);

        List<Classification> classifications = resolveClassificationsWithCountry(dto.getClassificationIds());
        List<Actor> actors = resolverService.resolveEntities(
                resolverService.extractIds(actorRequests, AssignActorRequest::getActorId),
                actorRepository::findAllById, "actors");
        List<Category> categories = resolverService.resolveEntities(
                dto.getCategories(), categoryRepository::findAllById, "categories");
        List<People> peoples = resolverService.resolveEntities(
                resolverService.extractIds(peopleRequests, AssignPeopleRequest::getPeopleId),
                peopleRepository::findAllById, "people");

        validateUniquePrincipalPoster(dto.getPosters());

        Movie movie = movieRepository.save(dto.createEntity());

        saveMovieCountryInfo(movie, classifications);
        saveCast(movie, actors, actorRequests);
        saveMovieCategories(movie, categories);
        savePosters(movie, dto.getPosters());
        saveMoviePeople(movie, peoples, peopleRequests);
    }

    @Override
    @Transactional
    public void updateMovie(UUID movieId, UpdateMovieRequest dto) throws ResourceNotFoundException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        if (dto.getTitle() != null) movie.setTitle(dto.getTitle());
        if (dto.getSynopsis() != null) movie.setSynopsis(dto.getSynopsis());
        if (dto.getDuration() != null) movie.setDuration(dto.getDuration());
        if (dto.getTrailerLink() != null) movie.setTrailerLink(dto.getTrailerLink());
        if (dto.getOriginalLanguage() != null) movie.setOriginalLanguage(dto.getOriginalLanguage());
        if (dto.getReleaseDate() != null) movie.setReleaseDate(dto.getReleaseDate());

        movieRepository.save(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDetailResponse> findAllMoviesByCountry(UUID countryId) {
        List<Movie> movies = movieRepository.findActiveByCountryId(countryId);
        if (movies.isEmpty()) return Collections.emptyList();

        List<UUID> movieIds = movies.stream().map(Movie::getId).toList();

        Map<UUID, List<Cast>> castByMovieId = castRepository
                .findWithActorByMovieIdIn(movieIds)
                .stream().collect(Collectors.groupingBy(c -> c.getMovie().getId()));

        Map<UUID, List<MovieCountryInfo>> classifByMovieId = movieCountryInfoRepository
                .findActiveByCountryAndMovieIdIn(movieIds, countryId)
                .stream().collect(Collectors.groupingBy(mci -> mci.getMovie().getId()));

        Map<UUID, List<MovieCategory>> categoriesByMovieId = movieCategoryRepository
                .findWithCategoryByMovieIdIn(movieIds)
                .stream().collect(Collectors.groupingBy(mc -> mc.getMovie().getId()));

        Map<UUID, List<Poster>> postersByMovieId = posterRepository
                .findByMovie_IdIn(movieIds)
                .stream().collect(Collectors.groupingBy(p -> p.getMovie().getId()));

        Map<UUID, List<MoviePeople>> peopleByMovieId = moviePeopleRepository
                .findWithPeopleByMovieIdIn(movieIds)
                .stream().collect(Collectors.groupingBy(mp -> mp.getMovie().getId()));

        return movies.stream()
                .map(movie -> MovieDetailResponse.from(
                        movie,
                        castByMovieId.getOrDefault(movie.getId(), Collections.emptyList()),
                        classifByMovieId.getOrDefault(movie.getId(), Collections.emptyList()),
                        categoriesByMovieId.getOrDefault(movie.getId(), Collections.emptyList()),
                        postersByMovieId.getOrDefault(movie.getId(), Collections.emptyList()),
                        peopleByMovieId.getOrDefault(movie.getId(), Collections.emptyList())
                ))
                .toList();
    }

    private List<Classification> resolveClassificationsWithCountry(List<UUID> ids)
            throws ResourceNotFoundException, ConflictException {

        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<UUID> uniqueIds = ids.stream().distinct().toList();
        List<Classification> found = classificationRepository.findWithCountryByIdIn(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("One or more classifications do not exist");
        }

        Set<UUID> seenCountries = new HashSet<>();
        for (Classification c : found) {
            UUID countryId = c.getCountry().getId();
            if (!seenCountries.add(countryId)) {
                throw new ConflictException(
                        "A movie cannot have more than one classification per country. " +
                        "Country '" + c.getCountry().getName() + "' appears more than once.");
            }
        }
        return found;
    }

    private void validateUniquePrincipalPoster(List<CreatePosterRequest> posters)
            throws ConflictException {
        if (posters == null || posters.isEmpty()) return;
        long mainCount = posters.stream().filter(CreatePosterRequest::isMain).count();
        if (mainCount > 1) {
            throw new ConflictException("Only one main poster is allowed per movie");
        }
    }

    // Save Data
    private void saveMovieCountryInfo(Movie movie, List<Classification> classifications) {
        if (classifications.isEmpty()) return;

        List<MovieCountryInfo> list = classifications.stream()
                .map(c -> {
                    MovieCountryInfo mci = new MovieCountryInfo();
                    mci.setMovie(movie);
                    mci.setClassification(c);
                    // isActive defaults to true
                    return mci;
                })
                .toList();

        movieCountryInfoRepository.saveAll(list);
    }

    private void saveCast(Movie movie, List<Actor> actors, List<AssignActorRequest> requests) {
        if (actors.isEmpty()) return;

        Map<UUID, Actor> actorsById = actors.stream()
                .collect(Collectors.toMap(Actor::getId, Function.identity()));

        List<Cast> casts = requests.stream()
                .map(req -> req.createEntity(movie, actorsById.get(req.getActorId())))
                .toList();

        castRepository.saveAll(casts);
    }

    private void saveMovieCategories(Movie movie, List<Category> categories) {
        if (categories.isEmpty()) return;

        List<MovieCategory> movieCategories = categories.stream()
                .map(category -> {
                    MovieCategory mc = new MovieCategory();
                    mc.setMovie(movie);
                    mc.setCategory(category);
                    return mc;
                })
                .toList();

        movieCategoryRepository.saveAll(movieCategories);
    }

    private void savePosters(Movie movie, List<CreatePosterRequest> posters) {
        if (posters == null || posters.isEmpty()) return;

        List<Poster> posterList = posters.stream()
                .map(dto -> dto.createEntity(movie))
                .toList();

        posterRepository.saveAll(posterList);
    }

    private void saveMoviePeople(Movie movie, List<People> peoples, List<AssignPeopleRequest> requests) {
        if (peoples.isEmpty()) return;

        Map<UUID, People> peopleById = peoples.stream()
                .collect(Collectors.toMap(People::getId, Function.identity()));

        List<MoviePeople> moviePeopleList = requests.stream()
                .map(req -> req.createEntity(movie, peopleById.get(req.getPeopleId())))
                .toList();

        moviePeopleRepository.saveAll(moviePeopleList);
    }
}
