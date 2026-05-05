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
import com.example.movies.services.movie.inteface.MovieCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MovieCategoryServiceImplementation implements MovieCategoryService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final MovieCategoryRepository movieCategoryRepository;

    public MovieCategoryServiceImplementation(MovieRepository movieRepository,
                                              CategoryRepository categoryRepository,
                                              MovieCategoryRepository movieCategoryRepository) {
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.movieCategoryRepository = movieCategoryRepository;
    }

    @Override
    @Transactional
    public List<CategoryResponse> addCategory(UUID movieId, UUID categoryId)
            throws ResourceNotFoundException, ConflictException {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (movieCategoryRepository.existsByMovie_IdAndCategory_Id(movieId, categoryId)) {
            throw new ConflictException("Category '" + category.getName() + "' is already assigned to this movie");
        }

        MovieCategory movieCategory = new MovieCategory();
        movieCategory.setMovie(movie);
        movieCategory.setCategory(category);
        movieCategoryRepository.save(movieCategory);

        return getCategories(movieId);
    }

    @Override
    @Transactional
    public List<CategoryResponse> removeCategory(UUID movieId, UUID categoryId)
            throws ResourceNotFoundException, ConflictException {

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }

        if (movieCategoryRepository.countByMovie_Id(movieId) <= 1) {
            throw new ConflictException("Cannot remove the only category from the movie");
        }

        MovieCategory movieCategory = movieCategoryRepository.findByMovie_Id(movieId)
                .stream()
                .filter(mc -> mc.getCategory().getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found in this movie"));

        movieCategoryRepository.delete(movieCategory);

        return getCategories(movieId);
    }

    private List<CategoryResponse> getCategories(UUID movieId) {
        return movieCategoryRepository.findByMovie_Id(movieId)
                .stream()
                .map(mc -> CategoryResponse.from(mc.getCategory()))
                .toList();
    }
}
