package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.model.Category;
import project.service.CategoryService;
import project.web.admin.CategoriesAdminController;
import project.web.dto.CreateCategoryRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriesAdminControllerUnitTests {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoriesAdminController controller;

    @Test
    void getCategories_ShouldReturnModelAndView() {
        List<Category> list = List.of(new Category());
        when(categoryService.findAll()).thenReturn(list);

        ModelAndView mav = controller.getCategories();

        assertEquals("admin/categories", mav.getViewName());
        assertEquals(list, mav.getModel().get("categories"));
        assertEquals("categories", mav.getModel().get("page"));
        assertEquals("Categories", mav.getModel().get("title"));

        verify(categoryService).findAll();
    }

    @Test
    void getAddCategoryForm_ShouldReturnModelAndView() {
        ModelAndView mav = controller.createCategory();

        assertEquals("admin/category_form", mav.getViewName());
        assertInstanceOf(CreateCategoryRequest.class, mav.getModel().get("category"));
        assertEquals("categories", mav.getModel().get("page"));
        assertEquals("Categories", mav.getModel().get("title"));
    }

    @Test
    void createCategory_WithErrors_ShouldReturnForm() {
        CreateCategoryRequest req = new CreateCategoryRequest("Test");
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        ModelAndView mav = controller.createCategory(req, result, mock(RedirectAttributes.class));

        assertEquals("admin/category_form", mav.getViewName());
        assertEquals(req, mav.getModel().get("category"));
    }

    @Test
    void createCategory_Success_ShouldRedirect() {
        CreateCategoryRequest req = new CreateCategoryRequest("Test");
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        when(categoryService.create(req)).thenReturn(true);

        RedirectAttributes redirect = mock(RedirectAttributes.class);

        ModelAndView mav = controller.createCategory(req, result, redirect);

        assertEquals("redirect:/admin/categories", mav.getViewName());
        verify(categoryService).create(req);
    }

    @Test
    void createCategory_NameExists_ShouldReturnForm() {
        CreateCategoryRequest req = new CreateCategoryRequest("Test");
        BindingResult result = mock(BindingResult.class);

        when(result.hasErrors()).thenReturn(false);
        when(categoryService.create(req)).thenReturn(false);

        ModelAndView mav = controller.createCategory(req, result, mock(RedirectAttributes.class));

        assertEquals("admin/category_form", mav.getViewName());
        verify(result).rejectValue("name", "error.category", "A category with this name already exists.");
    }

    @Test
    void editCategory_ShouldReturnCategoryData() {
        UUID id = UUID.randomUUID();
        Category category = new Category();
        category.setId(id);
        category.setName("Test");

        when(categoryService.findById(id)).thenReturn(category);

        ModelAndView mav = controller.editCategory(id);

        assertEquals("admin/category_form", mav.getViewName());
        assertEquals(id, mav.getModel().get("category_id"));
    }

    @Test
    void editCategoryPost_WithErrors_ShouldReturnForm() {
        UUID id = UUID.randomUUID();
        CreateCategoryRequest req = new CreateCategoryRequest("Test");

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        ModelAndView mav = controller.editCategory(id, req, result, mock(RedirectAttributes.class));

        assertEquals("admin/category_form", mav.getViewName());
    }

    @Test
    void editCategoryPost_Success_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        CreateCategoryRequest req = new CreateCategoryRequest("Test");

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        ModelAndView mav = controller.editCategory(id, req, result, mock(RedirectAttributes.class));

        assertEquals("redirect:/admin/categories", mav.getViewName());
        verify(categoryService).edit(id, req);
    }

    @Test
    void deleteCategory_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        ModelAndView mav = controller.deleteCategory(id, redirect);

        assertEquals("redirect:/admin/categories", mav.getViewName());
        verify(categoryService).deleteById(id);
    }
}
