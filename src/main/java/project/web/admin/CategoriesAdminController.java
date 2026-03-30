package project.web.admin;

import jakarta.validation.Valid;
import project.model.Category;
import project.service.CategoryService;
import project.service.MessageService;
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
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/admin/categories")
public class CategoriesAdminController {
    private final CategoryService categoryService;
    private final Logger logger;
    private final MessageService messageService;

    public CategoriesAdminController(CategoryService categoryService, MessageService messageService) {
        this.categoryService = categoryService;
        this.messageService = messageService;
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
    public ModelAndView createCategory(Locale locale) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/category_form");
        modelAndView.addObject("category", new CreateCategoryRequest());
        modelAndView.addObject("page", "categories");
        modelAndView.addObject("title", "Categories");

        String message = messageService.getLocalizedMessage("form_categories", locale);
        logger.info(message);

        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createCategory(@Valid @ModelAttribute("category") CreateCategoryRequest createCategoryRequest, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Locale locale) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/category_form");
            mav.addObject("category", createCategoryRequest);
            mav.addObject("page", "categories");
            mav.addObject("title", "Categories");

            String message = messageService.getLocalizedMessage("errors_create_category", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }

        if (categoryService.create(createCategoryRequest)) {

            String message = messageService.getLocalizedMessage("category_name_created", locale);
            message = message.replace("{}", createCategoryRequest.getName());
            redirectAttributes.addFlashAttribute("message", message);

            message = messageService.getLocalizedMessage("category_created", locale);
            logger.info(message, createCategoryRequest.getName());

            return new ModelAndView("redirect:/admin/categories");
        } else {
            String message = messageService.getLocalizedMessage("category_exists", locale);
            bindingResult.rejectValue("name", "error.category", message);
            ModelAndView mav = new ModelAndView("admin/category_form");
            mav.addObject("category", createCategoryRequest);
            mav.addObject("page", "categories");
            mav.addObject("title", "Categories");

            message = messageService.getLocalizedMessage("errors_create_category", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCategory(@PathVariable("id") UUID id, Locale locale) {
        Category category = categoryService.findById(id);
        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest(category.getName());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/category_form");
        modelAndView.addObject("category", createCategoryRequest);
        modelAndView.addObject("category_id", category.getId());
        modelAndView.addObject("page", "categories");
        modelAndView.addObject("title", "Categories");

        String message = messageService.getLocalizedMessage("category_edit", locale);
        logger.info(message, category.getName());

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCategory(@PathVariable("id") UUID id, @Valid @ModelAttribute("category") CreateCategoryRequest createCategoryRequest,
                                     BindingResult bindingResult, RedirectAttributes redirectAttributes, Locale locale) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/category_form");
            mav.addObject("category", createCategoryRequest);
            mav.addObject("page", "categories");
            mav.addObject("title", "Categories");

            String message = messageService.getLocalizedMessage("errors_edit_category", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }

        categoryService.edit(id, createCategoryRequest);

        String message = messageService.getLocalizedMessage("category_saved", locale);
        message = message.replace("{}", createCategoryRequest.getName());
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("category_edited", locale);
        logger.info(message, createCategoryRequest.getName());

        return new ModelAndView("redirect:/admin/categories");
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView deleteCategory(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes, Locale locale) {
        categoryService.deleteById(id);

        String message = messageService.getLocalizedMessage("category_deleted", locale);
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("category_id_deleted", locale);
        logger.info(message, id);

        return new ModelAndView("redirect:/admin/categories");
    }
}
