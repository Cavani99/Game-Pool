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
import project.service.MessageService;
import project.web.admin.CategoriesAdminController;
import project.web.dto.CreateCategoryRequest;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriesAdminControllerUnitTests {

    @Mock
    private CategoryService categoryService;
    @Mock
    private MessageService messageService;

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
        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createCategory(locale);

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

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createCategory(req, result, mock(RedirectAttributes.class), locale);

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

        Locale locale = Locale.ENGLISH;
        when(messageService.getLocalizedMessage("category_name_created", locale)).thenReturn("Category {} created successfully!");
        ModelAndView mav = controller.createCategory(req, result, redirect, locale);

        assertEquals("redirect:/admin/categories", mav.getViewName());
        verify(categoryService).create(req);
    }

    @Test
    void createCategory_NameExists_ShouldReturnForm() {
        CreateCategoryRequest req = new CreateCategoryRequest("Test");
        BindingResult result = mock(BindingResult.class);

        when(result.hasErrors()).thenReturn(false);
        when(categoryService.create(req)).thenReturn(false);

        Locale locale = Locale.ENGLISH;
        when(messageService.getLocalizedMessage("category_exists", locale)).thenReturn("A category with this name already exists.");
        ModelAndView mav = controller.createCategory(req, result, mock(RedirectAttributes.class), locale);

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

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.editCategory(id, locale);

        assertEquals("admin/category_form", mav.getViewName());
        assertEquals(id, mav.getModel().get("category_id"));
    }

    @Test
    void editCategoryPost_WithErrors_ShouldReturnForm() {
        UUID id = UUID.randomUUID();
        CreateCategoryRequest req = new CreateCategoryRequest("Test");

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.editCategory(id, req, result, mock(RedirectAttributes.class), locale);

        assertEquals("admin/category_form", mav.getViewName());
    }

    @Test
    void editCategoryPost_Success_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        CreateCategoryRequest req = new CreateCategoryRequest("Test");

        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        Locale locale = Locale.ENGLISH;
        when(messageService.getLocalizedMessage("category_saved", locale)).thenReturn("Category {} saved successfully!");
        ModelAndView mav = controller.editCategory(id, req, result, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/categories", mav.getViewName());
        verify(categoryService).edit(id, req);
    }

    @Test
    void deleteCategory_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.deleteCategory(id, redirect, locale);

        assertEquals("redirect:/admin/categories", mav.getViewName());
        verify(categoryService).deleteById(id);
    }
}
