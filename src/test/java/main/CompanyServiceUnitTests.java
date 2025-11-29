package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.model.Company;
import project.repository.CompanyRepository;
import project.service.CompanyService;
import project.web.dto.CreateCompanyRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceUnitTests {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    public void whenCompanyNamesIsNew_thenReturnTrueOnCreate() {
        CreateCompanyRequest companyRequest = CreateCompanyRequest.builder()
                .name("Company1")
                .build();

        when(companyRepository.findByName("Company1"))
                .thenReturn(Optional.empty());

        boolean result = companyService.create(companyRequest);

        assertTrue(result);
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    public void whenCompanyNameExists_thenReturnFalseOnCreate() {
        CreateCompanyRequest companyRequest = CreateCompanyRequest.builder()
                .name("Company1")
                .build();

        when(companyRepository.findByName("Company1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Company()));

        boolean firstResult = companyService.create(companyRequest);
        boolean secondResult = companyService.create(companyRequest);

        assertTrue(firstResult);
        assertFalse(secondResult);
    }

    @Test
    public void whenCompanyEdited_verifyChanges() {
        Company company = new Company();
        UUID companyId = UUID.randomUUID();
        CreateCompanyRequest companyRequest = CreateCompanyRequest.builder()
                .name("Company1")
                .build();
        when(companyRepository.findById(any()))
                .thenReturn(Optional.of(company));

        companyService.edit(companyId, companyRequest);

        assertEquals("Company1", company.getName());
        assertNotNull(company.getUpdatedOn());

        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
    }

    @Test
    public void whenCompanyIdExistReturnCompany() {
        UUID companyId = UUID.randomUUID();

        Company company = new Company();
        company.setId(companyId);
        company.setName("Company1");

        companyRepository.save(company);
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        Company result = companyService.findById(companyId);

        assertNotNull(result);
        assertEquals(companyId, result.getId());
        assertEquals("Company1", result.getName());
        verify(companyRepository).findById(companyId);
    }

    @Test
    public void whenCompanyIdNotExistThrowRuntimeException() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(any()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> companyService.findById(companyId)
        );

        assertEquals("Company does not exist!", exception.getMessage());
    }

    @Test
    public void whenCompanyExists_thenDeleteById() {
        UUID companyId = UUID.randomUUID();

        Company company = new Company();
        company.setId(companyId);
        company.setName("Company1");

        companyService.deleteById(companyId);

        verify(companyRepository).deleteById(companyId);
    }

    @Test
    public void whenCompaniesExist_thenReturnListOfCompanies() {
        List<Company> mockedCompanies = List.of(
                new Company(), new Company(), new Company()
        );
        when(companyRepository.findAllById(any()))
                .thenReturn(mockedCompanies);

        List<UUID> companyIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<Company> companies = companyService.findByCompaniesList(companyIds);


        assertNotNull(companies);
        assertEquals(mockedCompanies.size(), companies.size());
        assertEquals(mockedCompanies, companies);

        verify(companyRepository).findAllById(companyIds);
    }
}
