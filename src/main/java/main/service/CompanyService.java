package main.service;

import main.model.Company;
import main.repository.CompanyRepository;
import main.web.dto.CreateCompanyRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> findAll() {
        return companyRepository.findAllByOrderByCreatedOnDesc();
    }

    public boolean create(CreateCompanyRequest createCompanyRequest) {
        Company company = new Company();

        company.setName(createCompanyRequest.getName());
        company.setCreatedOn(LocalDateTime.now());
        company.setUpdatedOn(LocalDateTime.now());

        Optional<Company> findByName = companyRepository.findByName(company.getName());

        if (findByName.isEmpty()) {
            companyRepository.save(company);
            return true;
        } else {
            return false;
        }
    }

    public void edit(UUID id, CreateCompanyRequest createCompanyRequest) {
        Company company = findById(id);

        company.setName(createCompanyRequest.getName());
        company.setUpdatedOn(LocalDateTime.now());

        companyRepository.save(company);
    }

    public Company findById(UUID id) {
        return companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company does not exist!"));
    }

    public void deleteById(UUID id) {
        companyRepository.deleteById(id);
    }
}
