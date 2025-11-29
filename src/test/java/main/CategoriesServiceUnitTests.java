package main;

import project.model.Category;

import project.repository.CategoryRepository;
import project.service.CategoryService;
import project.web.dto.CreateCategoryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoriesServiceUnitTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void categoriesFindAllIsSortedRight() {
        Category category1 = new Category();
        category1.setId(UUID.randomUUID());
        category1.setCreatedOn(LocalDateTime.now());

        Category category2 = new Category();
        category2.setId(UUID.randomUUID());
        category2.setCreatedOn(LocalDateTime.now().plusHours(5));

        when(categoryRepository.findAllByOrderByCreatedOnDesc())
                .thenReturn(List.of(category2, category1));

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedOn()
                .isAfter(result.get(1).getCreatedOn()));

        verify(categoryRepository).findAllByOrderByCreatedOnDesc();
    }

    @Test
    public void whenCategoryNamesIsNew_thenReturnTrueOnCreate() {
        CreateCategoryRequest categoryRequest = CreateCategoryRequest.builder()
                .name("Category1")
                .build();

        // Repository returns empty -> name does NOT exist
        when(categoryRepository.findByName("Category1"))
                .thenReturn(Optional.empty());

        boolean result = categoryService.create(categoryRequest);

        assertTrue(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    public void whenCategoryNameExists_thenReturnFalseOnCreate() {
        CreateCategoryRequest categoryRequest = CreateCategoryRequest.builder()
                .name("Category1")
                .build();

        when(categoryRepository.findByName("Category1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Category()));

        boolean firstResult = categoryService.create(categoryRequest);
        boolean secondResult = categoryService.create(categoryRequest);

        assertTrue(firstResult);
        assertFalse(secondResult);
    }

    @Test
    public void whenCategoryEdited_verifyChanges() {
        Category category = new Category();
        UUID categoryId = UUID.randomUUID();
        CreateCategoryRequest categoryRequest = CreateCategoryRequest.builder()
                .name("Category1")
                .build();
        when(categoryRepository.findById(any()))
                .thenReturn(Optional.of(category));

        categoryService.edit(categoryId, categoryRequest);

        assertEquals("Category1", category.getName());
        assertNotNull(category.getUpdatedOn());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(category);
    }

    @Test
    public void whenCategoryIdExistReturnCategory() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Cat");

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        Category result = categoryService.findById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals("Cat", result.getName());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    public void whenCategoryIdNotExistThrowRuntimeException() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(any()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> categoryService.findById(categoryId)
        );

        assertEquals("Category does not exist!", exception.getMessage());
    }

    @Test
    public void whenCategoryExists_thenDeleteById() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Cat");

        categoryService.deleteById(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    public void whenCategoriesExist_thenReturnListOfCategories() {
        List<Category> mockedCategories = List.of(
                new Category(), new Category(), new Category()
        );
        when(categoryRepository.findAllById(any()))
                .thenReturn(mockedCategories);

        List<UUID> categoryIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<Category> categories = categoryService.findByCategoriesList(categoryIds);


        assertNotNull(categories);
        assertEquals(mockedCategories.size(), categories.size());
        assertEquals(mockedCategories, categories);

        verify(categoryRepository).findAllById(categoryIds);
    }

}
