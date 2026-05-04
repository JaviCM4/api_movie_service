package com.example.movies.services.movie;

import com.example.movies.dtos.movie.*;
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

    @Mock private MovieRepository            movieRepository;
    @Mock private ClassificationRepository   classificationRepository;
    @Mock private MovieCountryInfoRepository movieCountryInfoRepository;
    @Mock private ActorRepository            actorRepository;
    @Mock private CategoryRepository         categoryRepository;
    @Mock private PeopleRepository           peopleRepository;
    @Mock private CastRepository             castRepository;
    @Mock private MovieCategoryRepository    movieCategoryRepository;
    @Mock private PosterRepository           posterRepository;
    @Mock private MoviePeopleRepository      moviePeopleRepository;
    @Spy  private ResolverService            resolverService;

    @InjectMocks
    private MovieServiceImplementation movieService;

    // ── createMovie ────────────────────────────────────────────────────────

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

        when(classificationRepository.findWithCountryByIdIn(anyList()))
                .thenReturn(List.of(buildClassification(CLASSIF_USA_ID, buildCountry(COUNTRY_ID_USA, "United States"), "PG-13", 13),
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

        // Assert: gana el primero, el duplicado se descarta
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

    // ── Builders ──────────────────────────────────────────────────────────

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
        Movie m = new Movie();
        m.setId(SAVED_MOVIE_ID);
        m.setTitle("Inception");
        return m;
    }

    private Country buildCountry(UUID id, String name) {
        Country c = new Country();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private Classification buildClassification(UUID id, Country country, String name, int ageLimit) {
        Classification c = new Classification();
        c.setId(id);
        c.setCountry(country);
        c.setName(name);
        c.setAgeLimit(ageLimit);
        return c;
    }

    private Actor buildActor(UUID id, String name) {
        Actor a = new Actor();
        a.setId(id);
        a.setName(name);
        return a;
    }

    private Category buildCategory(UUID id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private People buildPeople(UUID id, String name) {
        People p = new People();
        p.setId(id);
        p.setName(name);
        return p;
    }
}
