package main;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.Application;
import project.model.Category;
import project.model.Company;
import project.model.Discount;
import project.model.Game;
import project.repository.CategoryRepository;
import project.repository.CompanyRepository;
import project.repository.DiscountRepository;
import project.repository.GameRepository;
import project.service.GameService;
import project.web.dto.CreateGameRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Transactional
class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void whenCreateGame_thenGameAndDiscountPersisted() {
        Category category = new Category();
        category.setName("Action");
        category.setCreatedOn(LocalDateTime.now());
        category.setUpdatedOn(LocalDateTime.now());
        categoryRepository.save(category);

        Company company = new Company();
        company.setName("Rockstar");
        company.setCreatedOn(LocalDateTime.now());
        company.setUpdatedOn(LocalDateTime.now());
        companyRepository.save(company);

        CreateGameRequest req = new CreateGameRequest(
                "RDR2",
                "Best game",
                null,
                category.getId(),
                company.getId(),
                "/uploads/games/rdr.jpg",
                50.0
        );

        boolean isCreated = gameService.create(req, category, company, "/uploads/games/rdr.jpg");

        assertTrue(isCreated);

        Game saved = gameRepository.findByTitle("RDR2").orElse(null);
        assertNotNull(saved);
        assertEquals("RDR2", saved.getTitle());
        assertEquals(50.0, saved.getPrice());
        assertNotNull(saved.getDiscount());

        Discount discount = saved.getDiscount();
        assertEquals(0, discount.getAmount());
    }
}

