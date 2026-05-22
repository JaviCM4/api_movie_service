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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MovieCategoryServiceImplementation implements MovieCategoryService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final MovieCategoryRepository movieCategoryRepository;

    @Autowired
    public MovieCategoryServiceImplementation(MovieRepository movieRepository, CategoryRepository categoryRepository, MovieCategoryRepository movieCategoryRepository) {
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.movieCategoryRepository = movieCategoryRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CategoryResponse> addCategory(UUID movieId, UUID categoryId)
            throws ResourceNotFoundException, ConflictException {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada con id: " + movieId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + categoryId));

        if (movieCategoryRepository.existsByMovie_IdAndCategory_Id(movieId, categoryId)) {
            throw new ConflictException("La categoría '" + category.getName() + "' ya está asignada a esta película");
        }

        MovieCategory movieCategory = new MovieCategory();
        movieCategory.setMovie(movie);
        movieCategory.setCategory(category);
        movieCategoryRepository.save(movieCategory);

        return getCategoryList(movieId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CategoryResponse> removeCategory(UUID movieId, UUID categoryId)
            throws ResourceNotFoundException, ConflictException {

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Película no encontrada con id: " + movieId);
        }

        if (movieCategoryRepository.countByMovie_Id(movieId) <= 1) {
            throw new ConflictException("No se puede eliminar la única categoría de la película");
        }

        MovieCategory movieCategory = movieCategoryRepository.findByMovie_Id(movieId)
                .stream()
                .filter(mc -> mc.getCategory().getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada en esta película"));

        movieCategoryRepository.delete(movieCategory);
        return getCategoryList(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(UUID movieId) throws ResourceNotFoundException {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Película no encontrada con id: " + movieId);
        }
        return getCategoryList(movieId);
    }

    private List<CategoryResponse> getCategoryList(UUID movieId) {
        return movieCategoryRepository.findByMovie_Id(movieId)
                .stream()
                .map(mc -> CategoryResponse.from(mc.getCategory()))
                .toList();
    }
}
