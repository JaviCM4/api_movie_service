package com.example.movies.services.category;

import com.example.movies.dtos.category.request.CreateCategoryRequest;
import com.example.movies.dtos.category.request.UpdateCategoryRequest;
import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.category.Category;
import com.example.movies.repositories.category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImplementation implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImplementation(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse createCategory(CreateCategoryRequest dto) throws ConflictException {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Category already exists with name: " + dto.getName());
        }

        Category category = new Category();
        category.setName(dto.getName());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest dto)
            throws ResourceNotFoundException, ConflictException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new ConflictException("Category already exists with name: " + dto.getName());
        }

        category.setName(dto.getName());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse toggleActive(UUID id) throws ResourceNotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(!category.isActive());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllActive() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
