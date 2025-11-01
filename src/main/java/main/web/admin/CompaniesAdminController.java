package main.web.admin;

import jakarta.validation.Valid;
import main.model.Company;
import main.service.CompanyService;
import main.web.dto.CreateCompanyRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/companies")
public class CompaniesAdminController {
    private final CompanyService companyService;

    public CompaniesAdminController(CompanyService companyService) {
        this.companyService = companyService;
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
    public ModelAndView createCompany() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/company_form");
        modelAndView.addObject("company", new CreateCompanyRequest());
        modelAndView.addObject("page", "companies");
        modelAndView.addObject("title", "Companies");

        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createCompany(@Valid @ModelAttribute("company") CreateCompanyRequest createCompanyRequest, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/company_form");
            mav.addObject("company", createCompanyRequest);
            mav.addObject("page", "companies");
            mav.addObject("title", "Companies");
            return mav;
        }

        if (companyService.create(createCompanyRequest)) {

            redirectAttributes.addFlashAttribute("message", "Company " + createCompanyRequest.getName() + " created successfully!");

            return new ModelAndView("redirect:/admin/companies");
        } else {
            bindingResult.rejectValue("name", "error.company", "A company with this name already exists.");
            ModelAndView mav = new ModelAndView("admin/company_form");
            mav.addObject("company", createCompanyRequest);
            mav.addObject("page", "companies");
            mav.addObject("title", "Companies");
            return mav;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCompany(@PathVariable("id") UUID id) {
        Company company = companyService.findById(id);
        CreateCompanyRequest createCompanyRequest = new CreateCompanyRequest(company.getName());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/company_form");
        modelAndView.addObject("company", createCompanyRequest);
        modelAndView.addObject("company_id", company.getId());
        modelAndView.addObject("page", "companies");
        modelAndView.addObject("title", "Companies");

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editCompany(@PathVariable("id") UUID id, @Valid @ModelAttribute("company") CreateCompanyRequest createCompanyRequest,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/company_form");
            mav.addObject("company", createCompanyRequest);
            mav.addObject("page", "companies");
            mav.addObject("title", "Companies");
            return mav;
        }

        companyService.edit(id, createCompanyRequest);

        redirectAttributes.addFlashAttribute("message", "Company " + createCompanyRequest.getName() + " saved successfully!");

        return new ModelAndView("redirect:/admin/companies");
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView deleteCompany(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        companyService.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "Company deleted!");

        return new ModelAndView("redirect:/admin/companies");
    }
}
