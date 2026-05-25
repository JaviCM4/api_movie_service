package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.*;
import com.example.movies.dtos.movie.response.MovieAdminResponse;
import com.example.movies.dtos.movie.response.MovieBriefResponse;
import com.example.movies.dtos.movie.response.MovieDetailResponse;
import com.example.movies.dtos.movie.response.MovieSummaryResponse;
import com.example.movies.events.CreatedMovieEvent;
import com.example.movies.events.UpdatedMovieEvent;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.kafka.MovieKafkaProducer;
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
import com.example.movies.services.movie.inteface.MovieService;
import com.example.movies.services.utils.ResolverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
@Slf4j
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

    private final MovieKafkaProducer kafkaProducer;

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
            ResolverService resolverService, MovieKafkaProducer kafkaProducer) {

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
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMovie(CreateMovieRequest dto)
            throws ResourceNotFoundException, ConflictException {
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

        //Publicamos el evento de creacion de la pelicula
        try {
            CreatedMovieEvent event = CreatedMovieEvent.from(movie.getId(), movie.getTitle());
            kafkaProducer.sendCreatedMovieEvent(event);
        } catch (Exception e) {
            log.error("Error al publicar evento de creacion de pelicula id={}: {}", movie.getId(), e.getMessage());
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMovie(UUID movieId, UpdateMovieRequest dto)
            throws ResourceNotFoundException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada con id: " + movieId));

        if (dto.getTitle() != null) movie.setTitle(dto.getTitle());
        if (dto.getSynopsis() != null) movie.setSynopsis(dto.getSynopsis());
        if (dto.getDuration() != null) movie.setDuration(dto.getDuration());
        if (dto.getTrailerLink() != null) movie.setTrailerLink(dto.getTrailerLink());
        if (dto.getOriginalLanguage() != null) movie.setOriginalLanguage(dto.getOriginalLanguage());
        if (dto.getReleaseDate() != null) movie.setReleaseDate(dto.getReleaseDate());
        if (dto.getAllowComments() != null) movie.setAllowComments(dto.getAllowComments());
        if (dto.getAllowRatings() != null) movie.setAllowRatings(dto.getAllowRatings());

        movieRepository.save(movie);

        //Publicamos el evento de actualizacion
        try {
            UpdatedMovieEvent event = UpdatedMovieEvent.from(movie.getId(), movie.getTitle());
            kafkaProducer.sendUpdatedMovieEvent(event);
        }catch (Exception e) {
            log.error("Error al publicar evento de actualizacion de pelicula id={}: {}", movie.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSummaryResponse> findAllMoviesByCountry(UUID countryId, String title, UUID categoryId, UUID classificationId, String sort) {
        List<Movie> movies = movieRepository.findActiveByCountryIdWithFilters(countryId, title, categoryId, classificationId);

        if (movies.isEmpty()) return Collections.emptyList();

        List<UUID> movieIds = movies.stream().map(Movie::getId).toList();

        Map<UUID, List<MovieCountryInfo>> classifByMovieId = movieCountryInfoRepository
                .findActiveByCountryAndMovieIdIn(movieIds, countryId)
                .stream().collect(Collectors.groupingBy(mci -> mci.getMovie().getId()));

        Map<UUID, List<Poster>> postersByMovieId = posterRepository
                .findByMovie_IdIn(movieIds)
                .stream().collect(Collectors.groupingBy(p -> p.getMovie().getId()));

        List<MovieSummaryResponse> result = movies.stream()
                .map(movie -> MovieSummaryResponse.from(
                        movie,
                        classifByMovieId.getOrDefault(movie.getId(), Collections.emptyList()),
                        postersByMovieId.getOrDefault(movie.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        if ("releaseDate".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparing(MovieSummaryResponse::getReleaseDate,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public MovieDetailResponse findMovieById(UUID movieId, UUID countryId) throws ResourceNotFoundException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada con id: " + movieId));

        List<UUID> ids = List.of(movieId);

        List<Cast> casts = castRepository.findWithActorByMovieIdIn(ids);
        List<MovieCountryInfo> classifs = movieCountryInfoRepository.findActiveByCountryAndMovieIdIn(ids, countryId);
        List<MovieCategory> categories = movieCategoryRepository.findWithCategoryByMovieIdIn(ids);
        List<Poster> posters = posterRepository.findByMovie_IdIn(ids);
        List<MoviePeople> crew = moviePeopleRepository.findWithPeopleByMovieIdIn(ids);

        return MovieDetailResponse.from(movie, casts, classifs, categories, posters, crew);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieAdminResponse findMovieAdminById(UUID movieId) throws ResourceNotFoundException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada con id: " + movieId));
        return MovieAdminResponse.from(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieBriefResponse> findMoviesBrief(List<UUID> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) return List.of();
        List<Movie> movies = movieRepository.findAllById(movieIds);
        List<Poster> posters = posterRepository.findByMovie_IdIn(movieIds);
        List<MovieCountryInfo> classifs = movieCountryInfoRepository.findActiveByMovieIdIn(movieIds);
        Map<UUID, List<Poster>> postersByMovieId = posters.stream()
                .collect(Collectors.groupingBy(p -> p.getMovie().getId()));
        Map<UUID, List<MovieCountryInfo>> classifsByMovieId = classifs.stream()
                .collect(Collectors.groupingBy(mci -> mci.getMovie().getId()));
        return movies.stream()
                .map(m -> MovieBriefResponse.from(
                        m,
                        postersByMovieId.getOrDefault(m.getId(), List.of()),
                        classifsByMovieId.getOrDefault(m.getId(), List.of())))
                .toList();
    }

    private List<Classification> resolveClassificationsWithCountry(List<UUID> ids)
            throws ResourceNotFoundException, ConflictException {

        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<UUID> uniqueIds = ids.stream().distinct().toList();
        List<Classification> found = classificationRepository.findWithCountryByIdIn(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Una o más clasificaciones no existen");
        }

        Set<UUID> seenCountries = new HashSet<>();
        for (Classification c : found) {
            UUID countryId = c.getCountry().getId();
            if (!seenCountries.add(countryId)) {
                throw new ConflictException(
                        "Una película no puede tener más de una clasificación por país. " +
                        "El país '" + c.getCountry().getName() + "' aparece más de una vez.");
            }
        }
        return found;
    }

    private void validateUniquePrincipalPoster(List<CreatePosterRequest> posters)
            throws ConflictException {
        if (posters == null || posters.isEmpty()) return;
        long mainCount = posters.stream().filter(CreatePosterRequest::isMain).count();
        if (mainCount > 1) {
            throw new ConflictException("Solo se permite un póster principal por película");
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
