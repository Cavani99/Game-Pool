package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import project.Application;
import project.repository.DiscountRepository;
import project.repository.GameRepository;
import project.service.GameService;
import project.web.dto.CreateGameRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
public class GameCreationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @BeforeEach
    void setup() {
        gameRepository.deleteAll();
    }
/*
    @Test
    public void whenNewGameIsCreated_thenDefaultDiscountIsCreated() {
        UUID categoryId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        CreateGameRequest createGameRequest = new CreateGameRequest("RDR2", "Best Game", null,
                categoryId, companyId, "/uploads/games/rdr.jpg", 50.00);

        boolean gameIsSaved = gameService.create(createGameRequest, null, null, "/uploads/games/rdr.jpg");

        assertNotNull(gameIsSaved);

    }*/

}
