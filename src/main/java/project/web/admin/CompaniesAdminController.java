package project.web.admin;

import jakarta.validation.Valid;
import project.model.Company;
import project.service.CompanyService;
import project.service.MessageService;
import project.web.dto.CreateCompanyRequest;
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
@RequestMapping("/admin/companies")
public class CompaniesAdminController {
    private final CompanyService companyService;
    private final Logger logger;
    private final MessageService messageService;

    public CompaniesAdminController(CompanyService companyService, MessageService messageService) {
        this.companyService = companyService;
        this.messageService = messageService;
        this.logger = LoggerFactory.getLogger(CompaniesAdminController.class);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView getCompanies() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/companies");

        List<Company> companyList = companyService.findAll();

        modelAndView.addObject("companies", companyList);
        modelAndView.addObject("page", "companies");
        modelAndView.addObject("title", "Companies");

        return modelAndView;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createCompany(Locale locale) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/company_form");
        modelAndView.addObject("company", new CreateCompanyRequest());
        modelAndView.addObject("page", "companies");
        modelAndView.addObject("title", "Companies");

        String message = messageService.getLocalizedMessage("form_companies", locale);
        logger.info(message);

        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createCompany(@Valid @ModelAttribute("company") CreateCompanyRequest createCompanyRequest, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes, Locale locale) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/company_form");
            mav.addObject("company", createCompanyRequest);
            mav.addObject("page", "companies");
            mav.addObject("title", "Companies");

            String message = messageService.getLocalizedMessage("errors_create_company", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }

        if (companyService.create(createCompanyRequest)) {

            String message = messageService.getLocalizedMessage("company_name_created", locale);
            message = message.replace("{}", createCompanyRequest.getName());
            redirectAttributes.addFlashAttribute("message", message);

            message = messageService.getLocalizedMessage("company_created", locale);
            logger.info(message, createCompanyRequest.getName());

            return new ModelAndView("redirect:/admin/companies");
        } else {
            String message = messageService.getLocalizedMessage("company_exists", locale);
            bindingResult.rejectValue("name", "error.company", message);
            ModelAndView mav = new ModelAndView("admin/company_form");
            mav.addObject("company", createCompanyRequest);
            mav.addObject("page", "companies");
            mav.addObject("title", "Companies");

            message = messageService.getLocalizedMessage("errors_create_company", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCompany(@PathVariable("id") UUID id, Locale locale) {
        Company company = companyService.findById(id);
        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest(company.getName());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/company_form");
        modelAndView.addObject("company", createCompanyRequest);
        modelAndView.addObject("company_id", company.getId());
        modelAndView.addObject("page", "companies");
        modelAndView.addObject("title", "Companies");

        String message = messageService.getLocalizedMessage("company_edit", locale);
        logger.info(message, company.getName());

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCompany(@PathVariable("id") UUID id, @Valid @ModelAttribute("company") CreateCompanyRequest createCompanyRequest,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes, Locale locale) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/company_form");
            mav.addObject("company", createCompanyRequest);
            mav.addObject("page", "companies");
            mav.addObject("title", "Companies");

            String message = messageService.getLocalizedMessage("errors_edit_company", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }

        companyService.edit(id, createCompanyRequest);

        String message = messageService.getLocalizedMessage("company_saved", locale);
        message = message.replace("{}", createCompanyRequest.getName());
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("company_edited", locale);
        logger.info(message, createCompanyRequest.getName());

        return new ModelAndView("redirect:/admin/companies");
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView deleteCompany(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes, Locale locale) {
        companyService.deleteById(id);

        String message = messageService.getLocalizedMessage("company_deleted", locale);
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("company_id_deleted", locale);
        logger.info(message, id);

        return new ModelAndView("redirect:/admin/companies");
    }
}
