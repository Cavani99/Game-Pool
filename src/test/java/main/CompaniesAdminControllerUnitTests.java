package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.model.Company;
import project.service.CompanyService;
import project.service.MessageService;
import project.web.admin.CompaniesAdminController;
import project.web.dto.CreateCompanyRequest;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompaniesAdminControllerUnitTests {

    @Mock
    private CompanyService companyService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private CompaniesAdminController controller;

    @Test
    void getCompanies_ShouldReturnModelAndView() {
        List<Company> list = List.of(new Company());
        when(companyService.findAll()).thenReturn(list);

        ModelAndView mav = controller.getCompanies();

        assertEquals("admin/companies", mav.getViewName());
        assertEquals(list, mav.getModel().get("companies"));
    }

    @Test
    void getAddCompanyForm_ShouldReturnModelAndView() {
        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createCompany(locale);

        assertEquals("admin/company_form", mav.getViewName());
        assertInstanceOf(CreateCompanyRequest.class, mav.getModel().get("company"));
    }

    @Test
    void createCompany_WithErrors_ShouldReturnForm() {
        CreateCompanyRequest req = new CreateCompanyRequest("Test");
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createCompany(req, result, mock(RedirectAttributes.class), locale);

        assertEquals("admin/company_form", mav.getViewName());
    }

    @Test
    void createCompany_Success_ShouldRedirect() {
        CreateCompanyRequest req = new CreateCompanyRequest("Test");
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        when(companyService.create(req)).thenReturn(true);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createCompany(req, result, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/companies", mav.getViewName());
    }

    @Test
    void createCompany_NameExists_ShouldReturnForm() {
        CreateCompanyRequest req = new CreateCompanyRequest("Test");
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        when(companyService.create(req)).thenReturn(false);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createCompany(req, result, mock(RedirectAttributes.class), locale);

        assertEquals("admin/company_form", mav.getViewName());
        verify(result).rejectValue("name", "error.company", "A company with this name already exists.");
    }

    @Test
    void editCompany_ShouldReturnFormWithData() {
        UUID id = UUID.randomUUID();
        Company company = new Company();
        company.setId(id);
        company.setName("Test");

        when(companyService.findById(id)).thenReturn(company);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.editCompany(id, locale);

        assertEquals("admin/company_form", mav.getViewName());
        assertEquals(id, mav.getModel().get("company_id"));
    }

    @Test
    void editCompanyPost_WithErrors_ShouldReturnForm() {
        CreateCompanyRequest req = new CreateCompanyRequest("Test");
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.editCompany(UUID.randomUUID(), req, result, mock(RedirectAttributes.class), locale);

        assertEquals("admin/company_form", mav.getViewName());
    }

    @Test
    void editCompanyPost_Success_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        CreateCompanyRequest req = new CreateCompanyRequest("Test");
        BindingResult result = mock(BindingResult.class);

        when(result.hasErrors()).thenReturn(false);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.editCompany(id, req, result, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/companies", mav.getViewName());
        verify(companyService).edit(id, req);
    }

    @Test
    void deleteCompany_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.deleteCompany(id, redirect, locale);

        assertEquals("redirect:/admin/companies", mav.getViewName());
        verify(companyService).deleteById(id);
    }
}