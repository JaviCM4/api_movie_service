package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.*;
import com.example.movies.dtos.movie.response.MovieAdminResponse;
import com.example.movies.dtos.movie.response.MovieDetailResponse;
import com.example.movies.dtos.movie.response.MovieSummaryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.actor.Actor;
import com.example.movies.models.category.Category;
import com.example.movies.models.classification.Classification;
import com.example.movies.models.country.Country;
import com.example.movies.models.enums.RolMovieEnum;
import com.example.movies.models.movie.*;
import com.example.movies.models.people.People;
import com.example.movies.repositories.actor.ActorRepository;
import com.example.movies.repositories.category.CategoryRepository;
import com.example.movies.repositories.classification.ClassificationRepository;
import com.example.movies.repositories.movie.*;
import com.example.movies.repositories.people.PeopleRepository;
import com.example.movies.services.utils.ResolverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceImplTest {

    private static final UUID COUNTRY_ID_USA = UUID.randomUUID();
    private static final UUID COUNTRY_ID_MX  = UUID.randomUUID();
    private static final UUID CLASSIF_USA_ID = UUID.randomUUID();
    private static final UUID CLASSIF_MX_ID  = UUID.randomUUID();
    private static final UUID ACTOR_ID       = UUID.randomUUID();
    private static final UUID CATEGORY_ID    = UUID.randomUUID();
    private static final UUID PEOPLE_ID      = UUID.randomUUID();
    private static final UUID SAVED_MOVIE_ID = UUID.randomUUID();

    @Mock private MovieRepository movieRepository;
    @Mock private ClassificationRepository classificationRepository;
    @Mock private MovieCountryInfoRepository movieCountryInfoRepository;
    @Mock private ActorRepository actorRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PeopleRepository peopleRepository;
    @Mock private CastRepository castRepository;
    @Mock private MovieCategoryRepository movieCategoryRepository;
    @Mock private PosterRepository posterRepository;
    @Mock private MoviePeopleRepository moviePeopleRepository;
    @Spy  private ResolverService resolverService;

    @InjectMocks
    private MovieServiceImplementation movieService;

    @Test
    void testCreateMovie() throws Exception {
        // Arrange
        MovieServiceImplementation spy = spy(movieService);
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);

        CreateMovieRequest request = buildRequest(
                List.of(CLASSIF_USA_ID, CLASSIF_MX_ID),
                List.of(new AssignActorRequest(ACTOR_ID, "Dom Cobb")),
                List.of(CATEGORY_ID),
                List.of(new CreatePosterRequest("https://img.example.com/poster.jpg", true)),
                List.of(new AssignPeopleRequest(PEOPLE_ID, RolMovieEnum.DIRECTOR))
        );

        Movie savedMovie = buildSavedMovie();

        when(classificationRepository.findWithCountryByIdIn(anyList())).thenReturn(List.of(buildClassification(CLASSIF_USA_ID, buildCountry(COUNTRY_ID_USA, "United States"), "PG-13", 13),
                                    buildClassification(CLASSIF_MX_ID,  buildCountry(COUNTRY_ID_MX,  "Mexico"),        "B",     12)));
        when(actorRepository.findAllById(anyList())).thenReturn(List.of(buildActor(ACTOR_ID, "Leonardo DiCaprio")));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(buildCategory(CATEGORY_ID, "Sci-Fi")));
        when(peopleRepository.findAllById(anyList())).thenReturn(List.of(buildPeople(PEOPLE_ID, "Christopher Nolan")));
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);
        when(movieCountryInfoRepository.saveAll(anyList())).thenReturn(List.of());
        when(castRepository.saveAll(anyList())).thenReturn(List.of());
        when(movieCategoryRepository.saveAll(anyList())).thenReturn(List.of());
        when(posterRepository.saveAll(anyList())).thenReturn(List.of());
        when(moviePeopleRepository.saveAll(anyList())).thenReturn(List.of());

        // Act
        spy.createMovie(request);

        // Assert
        assertAll(
                () -> verify(movieRepository).save(movieCaptor.capture()),
                () -> assertEquals("Inception",    movieCaptor.getValue().getTitle()),
                () -> assertEquals("Una sinopsis", movieCaptor.getValue().getSynopsis()),
                () -> assertEquals(148,            movieCaptor.getValue().getDuration()),
                () -> assertEquals("English",      movieCaptor.getValue().getOriginalLanguage()),
                () -> verify(movieCountryInfoRepository).saveAll(anyList()),
                () -> verify(castRepository).saveAll(anyList()),
                () -> verify(movieCategoryRepository).saveAll(anyList()),
                () -> verify(posterRepository).saveAll(anyList()),
                () -> verify(moviePeopleRepository).saveAll(anyList())
        );
    }

    @Test
    void testCreateMovieSavesTwoMovieCountryInfos() throws Exception {
        // Arrange
        MovieServiceImplementation spy = spy(movieService);

        CreateMovieRequest request = buildRequest(
                List.of(CLASSIF_USA_ID, CLASSIF_MX_ID),
                List.of(), List.of(), List.of(), List.of()
        );

        when(classificationRepository.findWithCountryByIdIn(anyList()))
                .thenReturn(List.of(buildClassification(CLASSIF_USA_ID, buildCountry(COUNTRY_ID_USA, "United States"), "PG-13", 13),
                                    buildClassification(CLASSIF_MX_ID,  buildCountry(COUNTRY_ID_MX,  "Mexico"),        "B",     12)));
        when(movieRepository.save(any(Movie.class))).thenReturn(buildSavedMovie());

        ArgumentCaptor<List<MovieCountryInfo>> captor = ArgumentCaptor.forClass(List.class);
        when(movieCountryInfoRepository.saveAll(captor.capture())).thenReturn(List.of());

        // Act
        spy.createMovie(request);

        // Assert
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void testCreateMovieDeduplicatesActors() throws Exception {
        // Arrange
        MovieServiceImplementation spy = spy(movieService);

        CreateMovieRequest request = buildRequest(
                List.of(CLASSIF_USA_ID),
                List.of(new AssignActorRequest(ACTOR_ID, "Dom Cobb"),
                        new AssignActorRequest(ACTOR_ID, "Dominic Cobb")),  // duplicado
                List.of(), List.of(), List.of()
        );

        when(classificationRepository.findWithCountryByIdIn(anyList()))
                .thenReturn(List.of(buildClassification(CLASSIF_USA_ID, buildCountry(COUNTRY_ID_USA, "United States"), "PG-13", 13)));
        when(actorRepository.findAllById(anyList())).thenReturn(List.of(buildActor(ACTOR_ID, "Leonardo DiCaprio")));
        when(movieRepository.save(any(Movie.class))).thenReturn(buildSavedMovie());
        when(movieCountryInfoRepository.saveAll(anyList())).thenReturn(List.of());

        ArgumentCaptor<List<Cast>> castCaptor = ArgumentCaptor.forClass(List.class);
        when(castRepository.saveAll(castCaptor.capture())).thenReturn(List.of());

        // Act
        spy.createMovie(request);

        // Assert
        assertAll(
                () -> assertEquals(1, castCaptor.getValue().size()),
                () -> assertEquals("Dom Cobb", castCaptor.getValue().get(0).getCharacterName())
        );
    }

    @Test
    void testCreateMovieWhenTwoClassificationsSameCountry() {
        // Arrange
        UUID classifUsa2Id = UUID.randomUUID();
        Country usa = buildCountry(COUNTRY_ID_USA, "United States");

        CreateMovieRequest request = buildRequest(
                List.of(CLASSIF_USA_ID, classifUsa2Id),
                List.of(), List.of(), List.of(), List.of()
        );

        when(classificationRepository.findWithCountryByIdIn(anyList()))
                .thenReturn(List.of(buildClassification(CLASSIF_USA_ID, usa, "PG-13", 13),
                                    buildClassification(classifUsa2Id,  usa, "R",     17)));

        // Assert
        assertThrows(ConflictException.class, () -> movieService.createMovie(request));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void testCreateMovieWhenClassificationNotFound() {
        // Arrange
        CreateMovieRequest request = buildRequest(
                List.of(UUID.randomUUID()),
                List.of(), List.of(), List.of(), List.of()
        );

        when(classificationRepository.findWithCountryByIdIn(anyList())).thenReturn(List.of());

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.createMovie(request));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void testCreateMovieWhenMoreThanOneMainPoster() {
        // Arrange
        CreateMovieRequest request = buildRequest(
                List.of(CLASSIF_USA_ID),
                List.of(), List.of(),
                List.of(new CreatePosterRequest("https://img.example.com/a.jpg", true),
                        new CreatePosterRequest("https://img.example.com/b.jpg", true)),
                List.of()
        );

        when(classificationRepository.findWithCountryByIdIn(anyList()))
                .thenReturn(List.of(buildClassification(CLASSIF_USA_ID, buildCountry(COUNTRY_ID_USA, "United States"), "PG-13", 13)));

        // Assert
        assertThrows(ConflictException.class, () -> movieService.createMovie(request));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void testUpdateMovie() throws Exception {
        // Arrange
        MovieServiceImplementation spy = spy(movieService);
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);

        Movie existing = new Movie();
        existing.setId(SAVED_MOVIE_ID);
        existing.setTitle("Old Title");
        existing.setSynopsis("Old synopsis");
        existing.setDuration(90);
        existing.setOriginalLanguage("Spanish");

        UpdateMovieRequest request = new UpdateMovieRequest(
                "New Title",
                "New synopsis",
                120,
                "https://www.youtube.com/watch?v=newtrailer",
                "English",
                LocalDate.now().plusYears(1),
                null,
                null
        );

        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.of(existing));
        when(movieRepository.save(any(Movie.class))).thenReturn(existing);

        // Act
        spy.updateMovie(SAVED_MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(movieRepository).save(movieCaptor.capture()),
                () -> assertEquals("New Title",    movieCaptor.getValue().getTitle()),
                () -> assertEquals("New synopsis", movieCaptor.getValue().getSynopsis()),
                () -> assertEquals(120,            movieCaptor.getValue().getDuration()),
                () -> assertEquals("English",      movieCaptor.getValue().getOriginalLanguage())
        );
    }

    @Test
    void testUpdateMovieOnlyUpdatesNonNullFields() throws Exception {
        // Arrange
        MovieServiceImplementation spy = spy(movieService);
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);

        Movie existing = new Movie();
        existing.setId(SAVED_MOVIE_ID);
        existing.setTitle("Original Title");
        existing.setSynopsis("Original synopsis");
        existing.setDuration(90);

        // Solo actualiza el título, el resto queda null
        UpdateMovieRequest request = new UpdateMovieRequest(
                "Updated Title", null, null, null, null, null, null, null
        );

        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.of(existing));
        when(movieRepository.save(any(Movie.class))).thenReturn(existing);

        // Act
        spy.updateMovie(SAVED_MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(movieRepository).save(movieCaptor.capture()),
                () -> assertEquals("Updated Title",    movieCaptor.getValue().getTitle()),
                () -> assertEquals("Original synopsis", movieCaptor.getValue().getSynopsis()),
                () -> assertEquals(90,                  movieCaptor.getValue().getDuration())
        );
    }

    @Test
    void testUpdateMovieWhenMovieNotFound() {
        // Arrange
        UpdateMovieRequest request = new UpdateMovieRequest(
                "New Title", null, null, null, null, null, null, null
        );

        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieService.updateMovie(SAVED_MOVIE_ID, request));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void testFindAllMoviesByCountry() {
        // Arrange
        Movie movie1 = buildSavedMovie();
        Movie movie2 = new Movie();
        movie2.setId(UUID.randomUUID());
        movie2.setTitle("The Matrix");
        movie2.setReleaseDate(LocalDate.now().plusYears(2));

        Country usa = buildCountry(COUNTRY_ID_USA, "United States");
        Classification classif = buildClassification(CLASSIF_USA_ID, usa, "PG-13", 13);

        MovieCountryInfo mci = new MovieCountryInfo();
        mci.setMovie(movie1);
        mci.setClassification(classif);

        Poster poster = new Poster();
        poster.setMovie(movie1);
        poster.setUrlImage("https://img.example.com/poster.jpg");
        poster.setMain(true);

        when(movieRepository.findActiveByCountryIdWithFilters(COUNTRY_ID_USA, null, null, null))
                .thenReturn(List.of(movie1, movie2));
        when(movieCountryInfoRepository.findActiveByCountryAndMovieIdIn(anyList(), eq(COUNTRY_ID_USA)))
                .thenReturn(List.of(mci));
        when(posterRepository.findByMovie_IdIn(anyList())).thenReturn(List.of(poster));

        // Act
        List<MovieSummaryResponse> result = movieService.findAllMoviesByCountry(
                COUNTRY_ID_USA, null, null, null, null);

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Inception",  result.get(0).getTitle()),
                () -> assertEquals("The Matrix", result.get(1).getTitle()),
                () -> assertEquals("https://img.example.com/poster.jpg", result.get(0).getPoster()),
                () -> assertEquals(1, result.get(0).getClassifications().size()),
                () -> assertEquals("PG-13",         result.get(0).getClassifications().get(0).getName()),
                () -> assertEquals("United States", result.get(0).getClassifications().get(0).getCountry())
        );
    }

    @Test
    void testFindAllMoviesByCountryWithFilters() {
        // Arrange
        UUID categoryId      = CATEGORY_ID;
        UUID classificationId = CLASSIF_USA_ID;

        Movie movie1 = buildSavedMovie();

        Country usa = buildCountry(COUNTRY_ID_USA, "United States");
        Classification classif = buildClassification(CLASSIF_USA_ID, usa, "PG-13", 13);

        MovieCountryInfo mci = new MovieCountryInfo();
        mci.setMovie(movie1);
        mci.setClassification(classif);

        Poster poster = new Poster();
        poster.setMovie(movie1);
        poster.setUrlImage("https://img.example.com/poster.jpg");
        poster.setMain(true);

        when(movieRepository.findActiveByCountryIdWithFilters(COUNTRY_ID_USA, "Inception", categoryId, classificationId))
                .thenReturn(List.of(movie1));
        when(movieCountryInfoRepository.findActiveByCountryAndMovieIdIn(anyList(), eq(COUNTRY_ID_USA)))
                .thenReturn(List.of(mci));
        when(posterRepository.findByMovie_IdIn(anyList())).thenReturn(List.of(poster));

        // Act
        List<MovieSummaryResponse> result = movieService.findAllMoviesByCountry(
                COUNTRY_ID_USA, "Inception", categoryId, classificationId, null);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Inception", result.get(0).getTitle())
        );
    }

    @Test
    void testFindAllMoviesByCountrySortedByReleaseDate() {
        // Arrange
        Movie movie1 = buildSavedMovie();
        movie1.setReleaseDate(LocalDate.of(2027, 6, 1));

        Movie movie2 = new Movie();
        movie2.setId(UUID.randomUUID());
        movie2.setTitle("The Matrix");
        movie2.setReleaseDate(LocalDate.of(2026, 1, 1));

        when(movieRepository.findActiveByCountryIdWithFilters(COUNTRY_ID_USA, null, null, null))
                .thenReturn(List.of(movie1, movie2));
        when(movieCountryInfoRepository.findActiveByCountryAndMovieIdIn(anyList(), eq(COUNTRY_ID_USA)))
                .thenReturn(List.of());
        when(posterRepository.findByMovie_IdIn(anyList())).thenReturn(List.of());

        // Act
        List<MovieSummaryResponse> result = movieService.findAllMoviesByCountry(
                COUNTRY_ID_USA, null, null, null, "releaseDate");

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("The Matrix", result.get(0).getTitle()),
                () -> assertEquals("Inception",  result.get(1).getTitle())
        );
    }

    @Test
    void testFindAllMoviesByCountryReturnsEmptyWhenNoMovies() {
        // Arrange
        when(movieRepository.findActiveByCountryIdWithFilters(COUNTRY_ID_USA, null, null, null))
                .thenReturn(List.of());

        // Act
        List<MovieSummaryResponse> result = movieService.findAllMoviesByCountry(
                COUNTRY_ID_USA, null, null, null, null);

        // Assert
        assertAll(
                () -> assertTrue(result.isEmpty()),
                () -> verify(movieCountryInfoRepository, never()).findActiveByCountryAndMovieIdIn(anyList(), any()),
                () -> verify(posterRepository, never()).findByMovie_IdIn(anyList())
        );
    }

    @Test
    void testFindMovieById() throws Exception {
        // Arrange
        Movie movie = buildSavedMovie();

        Country usa = buildCountry(COUNTRY_ID_USA, "United States");
        Classification classif = buildClassification(CLASSIF_USA_ID, usa, "PG-13", 13);

        Actor actor = buildActor(ACTOR_ID, "Leonardo DiCaprio");
        Cast cast = new Cast();
        cast.setMovie(movie);
        cast.setActor(actor);
        cast.setCharacterName("Dom Cobb");

        MovieCountryInfo mci = new MovieCountryInfo();
        mci.setMovie(movie);
        mci.setClassification(classif);

        Category category = buildCategory(CATEGORY_ID, "Sci-Fi");
        MovieCategory mc = new MovieCategory();
        mc.setMovie(movie);
        mc.setCategory(category);

        Poster poster = new Poster();
        poster.setMovie(movie);
        poster.setUrlImage("https://img.example.com/poster.jpg");
        poster.setMain(true);

        People people = buildPeople(PEOPLE_ID, "Christopher Nolan");
        MoviePeople mp = new MoviePeople();
        mp.setMovie(movie);
        mp.setPeople(people);
        mp.setRol(RolMovieEnum.DIRECTOR);

        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.of(movie));
        when(castRepository.findWithActorByMovieIdIn(anyList())).thenReturn(List.of(cast));
        when(movieCountryInfoRepository.findActiveByCountryAndMovieIdIn(anyList(), eq(COUNTRY_ID_USA))).thenReturn(List.of(mci));
        when(movieCategoryRepository.findWithCategoryByMovieIdIn(anyList())).thenReturn(List.of(mc));
        when(posterRepository.findByMovie_IdIn(anyList())).thenReturn(List.of(poster));
        when(moviePeopleRepository.findWithPeopleByMovieIdIn(anyList())).thenReturn(List.of(mp));

        // Act
        MovieDetailResponse result = movieService.findMovieById(SAVED_MOVIE_ID, COUNTRY_ID_USA);

        // Assert
        assertAll(
                () -> assertEquals("Inception", result.getTitle()),
                () -> assertEquals(1, result.getCast().size()),
                () -> assertEquals("Leonardo DiCaprio", result.getCast().get(0).getActorName()),
                () -> assertEquals("Dom Cobb",          result.getCast().get(0).getCharacterName()),
                () -> assertEquals(1, result.getClassifications().size()),
                () -> assertEquals("PG-13",         result.getClassifications().get(0).getName()),
                () -> assertEquals("United States", result.getClassifications().get(0).getCountry()),
                () -> assertEquals(1, result.getCategories().size()),
                () -> assertEquals("Sci-Fi", result.getCategories().get(0)),
                () -> assertEquals(1, result.getPosters().size()),
                () -> assertTrue(result.getPosters().get(0).isMain()),
                () -> assertEquals(1, result.getCrew().size()),
                () -> assertEquals(RolMovieEnum.DIRECTOR, result.getCrew().get(0).getRole())
        );
    }

    @Test
    void testFindMovieByIdWhenNotFound() {
        // Arrange
        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieService.findMovieById(SAVED_MOVIE_ID, COUNTRY_ID_USA));
    }

    @Test
    void testUpdateMovieAllowFlags() throws Exception {
        // Arrange
        MovieServiceImplementation spy = spy(movieService);
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);

        Movie existing = new Movie();
        existing.setId(SAVED_MOVIE_ID);
        existing.setTitle("Inception");
        existing.setAllowComments(true);
        existing.setAllowRatings(true);

        UpdateMovieRequest request = new UpdateMovieRequest(
                null, null, null, null, null, null, false, false
        );

        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.of(existing));
        when(movieRepository.save(any(Movie.class))).thenReturn(existing);

        // Act
        spy.updateMovie(SAVED_MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(movieRepository).save(movieCaptor.capture()),
                () -> assertFalse(movieCaptor.getValue().isAllowComments()),
                () -> assertFalse(movieCaptor.getValue().isAllowRatings()),
                () -> assertEquals("Inception", movieCaptor.getValue().getTitle())
        );
    }

    @Test
    void testFindMovieAdminById() throws Exception {
        // Arrange
        Movie movie = buildSavedMovie();
        movie.setTitle("Inception");
        movie.setSynopsis("Una sinopsis");
        movie.setDuration(148);
        movie.setOriginalLanguage("English");
        movie.setReleaseDate(LocalDate.of(2027, 1, 1));
        movie.setAllowComments(true);
        movie.setAllowRatings(false);

        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.of(movie));

        // Act
        MovieAdminResponse result = movieService.findMovieAdminById(SAVED_MOVIE_ID);

        // Assert
        assertAll(
                () -> assertEquals(SAVED_MOVIE_ID,     result.getId()),
                () -> assertEquals("Inception",        result.getTitle()),
                () -> assertEquals("Una sinopsis",     result.getSynopsis()),
                () -> assertEquals(148,                result.getDuration()),
                () -> assertEquals("English",          result.getOriginalLanguage()),
                () -> assertEquals(LocalDate.of(2027, 1, 1), result.getReleaseDate()),
                () -> assertTrue(result.isAllowComments()),
                () -> assertFalse(result.isAllowRatings())
        );
    }

    @Test
    void testFindMovieAdminByIdWhenNotFound() {
        // Arrange
        when(movieRepository.findById(SAVED_MOVIE_ID)).thenReturn(java.util.Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieService.findMovieAdminById(SAVED_MOVIE_ID));
    }

    private CreateMovieRequest buildRequest(
            List<UUID> classificationIds,
            List<AssignActorRequest> actors,
            List<UUID> categories,
            List<CreatePosterRequest> posters,
            List<AssignPeopleRequest> people) {

        return new CreateMovieRequest(
                classificationIds,
                "Inception",
                "Una sinopsis",
                148,
                "https://www.youtube.com/watch?v=YoHD9XEInc0",
                "English",
                LocalDate.now().plusYears(1),
                actors,
                categories,
                posters,
                people
        );
    }

    private Movie buildSavedMovie() {
        Movie newMovie = new Movie();
        newMovie.setId(SAVED_MOVIE_ID);
        newMovie.setTitle("Inception");
        return newMovie;
    }

    private Country buildCountry(UUID id, String name) {
        Country newCountry = new Country();
        newCountry.setId(id);
        newCountry.setName(name);
        return newCountry;
    }

    private Classification buildClassification(UUID id, Country country, String name, int ageLimit) {
        Classification newClassification = new Classification();
        newClassification.setId(id);
        newClassification.setCountry(country);
        newClassification.setName(name);
        newClassification.setAgeLimit(ageLimit);
        return newClassification;
    }

    private Actor buildActor(UUID id, String name) {
        Actor newActor = new Actor();
        newActor.setId(id);
        newActor.setName(name);
        return newActor;
    }

    private Category buildCategory(UUID id, String name) {
        Category newCategory = new Category();
        newCategory.setId(id);
        newCategory.setName(name);
        return newCategory;
    }

    private People buildPeople(UUID id, String name) {
        People newPeople = new People();
        newPeople.setId(id);
        newPeople.setName(name);
        return newPeople;
    }
}
