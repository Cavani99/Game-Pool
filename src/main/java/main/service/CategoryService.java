package main.service;

import main.model.Category;
import main.repository.CategoryRepository;
import main.web.dto.CreateCategoryRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByCreatedOnDesc();
    }

    public boolean create(CreateCategoryRequest createCategoryRequest) {
        Category category = new Category();

        category.setName(createCategoryRequest.getName());
        category.setCreatedOn(LocalDateTime.now());
        category.setUpdatedOn(LocalDateTime.now());

        Optional<Category> findByName = categoryRepository.findByName(category.getName());

        if (findByName.isEmpty()) {
            categoryRepository.save(category);
            return true;
        } else {
            return false;
        }
    }

    public void edit(UUID id, CreateCategoryRequest createCategoryRequest) {
        Category category = findById(id);

        category.setName(createCategoryRequest.getName());
        category.setUpdatedOn(LocalDateTime.now());

        categoryRepository.save(category);
    }

    public Category findById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category does not exist!"));
    }

    public void deleteById(UUID id) {
        categoryRepository.deleteById(id);
    }
}
