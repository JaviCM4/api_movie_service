package com.example.movies.services.movie;

import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.category.Category;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieCategory;
import com.example.movies.repositories.category.CategoryRepository;
import com.example.movies.repositories.movie.MovieCategoryRepository;
import com.example.movies.repositories.movie.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieCategoryServiceImplTest {

    private static final UUID MOVIE_ID      = UUID.randomUUID();
    private static final UUID CATEGORY_ID_1 = UUID.randomUUID();
    private static final UUID CATEGORY_ID_2 = UUID.randomUUID();

    @Mock private MovieRepository        movieRepository;
    @Mock private CategoryRepository     categoryRepository;
    @Mock private MovieCategoryRepository movieCategoryRepository;

    @InjectMocks
    private MovieCategoryServiceImplementation movieCategoryService;

    // ── addCategory ───────────────────────────────────────────────────────

    @Test
    void testAddCategory() throws Exception {
        // Arrange
        ArgumentCaptor<MovieCategory> captor = ArgumentCaptor.forClass(MovieCategory.class);
        Movie    movie    = buildMovie();
        Category category = buildCategory(CATEGORY_ID_2, "Sci-Fi");

        MovieCategory existing  = buildMovieCategory(movie, buildCategory(CATEGORY_ID_1, "Action"));
        MovieCategory newMc     = buildMovieCategory(movie, category);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(categoryRepository.findById(CATEGORY_ID_2)).thenReturn(Optional.of(category));
        when(movieCategoryRepository.existsByMovie_IdAndCategory_Id(MOVIE_ID, CATEGORY_ID_2)).thenReturn(false);
        when(movieCategoryRepository.save(any(MovieCategory.class))).thenReturn(newMc);
        when(movieCategoryRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(existing, newMc));

        // Act
        List<CategoryResponse> result = movieCategoryService.addCategory(MOVIE_ID, CATEGORY_ID_2);

        // Assert
        assertAll(
                () -> verify(movieCategoryRepository).save(captor.capture()),
                () -> assertEquals(movie,    captor.getValue().getMovie()),
                () -> assertEquals(category, captor.getValue().getCategory()),
                () -> assertEquals(2, result.size())
        );
    }

    @Test
    void testAddCategoryWhenMovieNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCategoryService.addCategory(MOVIE_ID, CATEGORY_ID_1));
        verify(movieCategoryRepository, never()).save(any());
    }

    @Test
    void testAddCategoryWhenCategoryNotFound() {
        // Arrange
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(buildMovie()));
        when(categoryRepository.findById(CATEGORY_ID_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCategoryService.addCategory(MOVIE_ID, CATEGORY_ID_1));
        verify(movieCategoryRepository, never()).save(any());
    }

    @Test
    void testAddCategoryWhenAlreadyAssigned() {
        // Arrange
        Movie    movie    = buildMovie();
        Category category = buildCategory(CATEGORY_ID_1, "Action");

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(categoryRepository.findById(CATEGORY_ID_1)).thenReturn(Optional.of(category));
        when(movieCategoryRepository.existsByMovie_IdAndCategory_Id(MOVIE_ID, CATEGORY_ID_1)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> movieCategoryService.addCategory(MOVIE_ID, CATEGORY_ID_1));
        verify(movieCategoryRepository, never()).save(any());
    }

    // ── removeCategory ────────────────────────────────────────────────────

    @Test
    void testRemoveCategory() throws Exception {
        // Arrange
        Movie    movie = buildMovie();
        Category cat1  = buildCategory(CATEGORY_ID_1, "Action");
        Category cat2  = buildCategory(CATEGORY_ID_2, "Sci-Fi");

        MovieCategory mc1 = buildMovieCategory(movie, cat1);
        MovieCategory mc2 = buildMovieCategory(movie, cat2);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(movieCategoryRepository.countByMovie_Id(MOVIE_ID)).thenReturn(2L);
        when(movieCategoryRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(mc1, mc2));

        // Act
        List<CategoryResponse> result = movieCategoryService.removeCategory(MOVIE_ID, CATEGORY_ID_1);

        // Assert
        assertAll(
                () -> verify(movieCategoryRepository).delete(mc1),
                () -> assertEquals(2, result.size())
        );
    }

    @Test
    void testRemoveCategoryWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> movieCategoryService.removeCategory(MOVIE_ID, CATEGORY_ID_1));
        verify(movieCategoryRepository, never()).delete(any());
    }

    @Test
    void testRemoveCategoryWhenOnlyOneCategory() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(movieCategoryRepository.countByMovie_Id(MOVIE_ID)).thenReturn(1L);

        // Assert
        assertThrows(ConflictException.class,
                () -> movieCategoryService.removeCategory(MOVIE_ID, CATEGORY_ID_1));
        verify(movieCategoryRepository, never()).delete(any());
    }

    @Test
    void testRemoveCategoryWhenCategoryNotInMovie() {
        // Arrange
        Movie    movie = buildMovie();
        Category cat2  = buildCategory(CATEGORY_ID_2, "Sci-Fi");
        MovieCategory mc2 = buildMovieCategory(movie, cat2);

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(movieCategoryRepository.countByMovie_Id(MOVIE_ID)).thenReturn(2L);
        when(movieCategoryRepository.findByMovie_Id(MOVIE_ID)).thenReturn(List.of(mc2));

        // Assert — CATEGORY_ID_1 no está en la lista
        assertThrows(ResourceNotFoundException.class,
                () -> movieCategoryService.removeCategory(MOVIE_ID, CATEGORY_ID_1));
        verify(movieCategoryRepository, never()).delete(any());
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private Movie buildMovie() {
        Movie m = new Movie();
        m.setId(MOVIE_ID);
        m.setTitle("Test Movie");
        return m;
    }

    private Category buildCategory(UUID id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setActive(true);
        return c;
    }

    private MovieCategory buildMovieCategory(Movie movie, Category category) {
        MovieCategory mc = new MovieCategory();
        mc.setMovie(movie);
        mc.setCategory(category);
        return mc;
    }
}
