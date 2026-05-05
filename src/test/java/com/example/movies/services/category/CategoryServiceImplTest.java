package com.example.movies.services.category;

import com.example.movies.dtos.category.request.CreateCategoryRequest;
import com.example.movies.dtos.category.request.UpdateCategoryRequest;
import com.example.movies.dtos.category.response.CategoryResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.category.Category;
import com.example.movies.repositories.category.CategoryRepository;
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
public class CategoryServiceImplTest {

    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImplementation categoryService;

    // ── createCategory ────────────────────────────────────────────────────

    @Test
    void testCreateCategory() throws Exception {
        // Arrange
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        CreateCategoryRequest request = new CreateCategoryRequest("Action");

        Category saved = buildCategory("Action", true);

        when(categoryRepository.existsByNameIgnoreCase("Action")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        // Act
        CategoryResponse result = categoryService.createCategory(request);

        // Assert
        assertAll(
                () -> verify(categoryRepository).save(captor.capture()),
                () -> assertEquals("Action", captor.getValue().getName()),
                () -> assertEquals(CATEGORY_ID, result.getId()),
                () -> assertEquals("Action", result.getName()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testCreateCategoryWhenNameAlreadyExists() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest("Action");
        when(categoryRepository.existsByNameIgnoreCase("Action")).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    // ── updateCategory ────────────────────────────────────────────────────

    @Test
    void testUpdateCategory() throws Exception {
        // Arrange
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        UpdateCategoryRequest request = new UpdateCategoryRequest("Drama");

        Category existing = buildCategory("Action", true);
        Category updated  = buildCategory("Drama", true);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Drama", CATEGORY_ID)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        // Act
        CategoryResponse result = categoryService.updateCategory(CATEGORY_ID, request);

        // Assert
        assertAll(
                () -> verify(categoryRepository).save(captor.capture()),
                () -> assertEquals("Drama", captor.getValue().getName()),
                () -> assertEquals("Drama", result.getName())
        );
    }

    @Test
    void testUpdateCategoryWhenNotFound() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("Drama");
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(CATEGORY_ID, request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testUpdateCategoryWhenNameConflict() {
        // Arrange
        UpdateCategoryRequest request = new UpdateCategoryRequest("Drama");

        Category existing = buildCategory("Action", true);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Drama", CATEGORY_ID)).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> categoryService.updateCategory(CATEGORY_ID, request));
        verify(categoryRepository, never()).save(any());
    }

    // ── toggleActive ──────────────────────────────────────────────────────

    @Test
    void testToggleActiveFromTrueToFalse() throws Exception {
        // Arrange
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        Category active   = buildCategory("Action", true);
        Category inactive = buildCategory("Action", false);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(active));
        when(categoryRepository.save(any(Category.class))).thenReturn(inactive);

        // Act
        CategoryResponse result = categoryService.toggleActive(CATEGORY_ID);

        // Assert
        assertAll(
                () -> verify(categoryRepository).save(captor.capture()),
                () -> assertFalse(captor.getValue().isActive()),
                () -> assertFalse(result.isActive())
        );
    }

    @Test
    void testToggleActiveFromFalseToTrue() throws Exception {
        // Arrange
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        Category inactive = buildCategory("Action", false);
        Category active   = buildCategory("Action", true);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(inactive));
        when(categoryRepository.save(any(Category.class))).thenReturn(active);

        // Act
        CategoryResponse result = categoryService.toggleActive(CATEGORY_ID);

        // Assert
        assertAll(
                () -> verify(categoryRepository).save(captor.capture()),
                () -> assertTrue(captor.getValue().isActive()),
                () -> assertTrue(result.isActive())
        );
    }

    @Test
    void testToggleActiveWhenNotFound() {
        // Arrange
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.toggleActive(CATEGORY_ID));
        verify(categoryRepository, never()).save(any());
    }

    // ── findAllActive ─────────────────────────────────────────────────────

    @Test
    void testFindAllActive() {
        // Arrange
        Category c1 = buildCategory("Action", true);
        Category c2 = buildCategory("Drama",  true);
        c2.setId(UUID.randomUUID());

        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(c1, c2));

        // Act
        List<CategoryResponse> result = categoryService.findAllActive();

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Action", result.get(0).getName()),
                () -> assertTrue(result.get(0).isActive()),
                () -> assertEquals("Drama",  result.get(1).getName()),
                () -> assertTrue(result.get(1).isActive())
        );
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private Category buildCategory(String name, boolean active) {
        Category c = new Category();
        c.setId(CATEGORY_ID);
        c.setName(name);
        c.setActive(active);
        return c;
    }
}
