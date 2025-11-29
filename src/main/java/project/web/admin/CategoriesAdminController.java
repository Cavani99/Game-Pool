package project.web.admin;

import jakarta.validation.Valid;
import project.model.Category;
import project.service.CategoryService;
import project.web.dto.CreateCategoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/categories")
public class CategoriesAdminController {
    private final CategoryService categoryService;
    private final Logger logger;

    public CategoriesAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
        this.logger = LoggerFactory.getLogger(CategoriesAdminController.class);
    }


    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView getCategories() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/categories");

        List<Category> categories = categoryService.findAll();

        modelAndView.addObject("categories", categories);
        modelAndView.addObject("page", "categories");
        modelAndView.addObject("title", "Categories");

        return modelAndView;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createCategory() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/category_form");
        modelAndView.addObject("category", new CreateCategoryRequest());
        modelAndView.addObject("page", "categories");
        modelAndView.addObject("title", "Categories");

        logger.info("Form for creating category is opened");

        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createCategory(@Valid @ModelAttribute("category") CreateCategoryRequest createCategoryRequest, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/category_form");
            mav.addObject("category", createCategoryRequest);
            mav.addObject("page", "categories");
            mav.addObject("title", "Categories");

            logger.error("Errors in creating category: {}", bindingResult.getAllErrors());

            return mav;
        }

        if (categoryService.create(createCategoryRequest)) {

            redirectAttributes.addFlashAttribute("message", "Category " + createCategoryRequest.getName() + " created successfully!");
            logger.info("Category {} created!", createCategoryRequest.getName());

            return new ModelAndView("redirect:/admin/categories");
        } else {
            bindingResult.rejectValue("name", "error.category", "A category with this name already exists.");
            ModelAndView mav = new ModelAndView("admin/category_form");
            mav.addObject("category", createCategoryRequest);
            mav.addObject("page", "categories");
            mav.addObject("title", "Categories");

            logger.error("Errors in creating category: {}", bindingResult.getAllErrors());

            return mav;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCategory(@PathVariable("id") UUID id) {
        Category category = categoryService.findById(id);
        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest(category.getName());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/category_form");
        modelAndView.addObject("category", createCategoryRequest);
        modelAndView.addObject("category_id", category.getId());
        modelAndView.addObject("page", "categories");
        modelAndView.addObject("title", "Categories");

        logger.info("Edit Category {}", category.getName());

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCategory(@PathVariable("id") UUID id, @Valid @ModelAttribute("category") CreateCategoryRequest createCategoryRequest,
                                     BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/category_form");
            mav.addObject("category", createCategoryRequest);
            mav.addObject("page", "categories");
            mav.addObject("title", "Categories");

            logger.error("Errors in editing category: {}", bindingResult.getAllErrors());

            return mav;
        }

        categoryService.edit(id, createCategoryRequest);

        redirectAttributes.addFlashAttribute("message", "Category " + createCategoryRequest.getName() + " saved successfully!");
        logger.info("Category {} edited!", createCategoryRequest.getName());

        return new ModelAndView("redirect:/admin/categories");
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView deleteCategory(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        categoryService.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "Category deleted!");
        logger.info("Category with id {} deleted!", id);

        return new ModelAndView("redirect:/admin/categories");
    }
}
