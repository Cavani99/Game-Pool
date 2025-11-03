package main.service;

import main.model.Category;
import main.model.Company;
import main.model.Discount;
import main.model.Game;
import main.repository.DiscountRepository;
import main.repository.GameRepository;
import main.web.dto.CreateGameRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final DiscountRepository discountRepository;

    public GameService(GameRepository gameRepository, DiscountRepository discountRepository) {
        this.gameRepository = gameRepository;
        this.discountRepository = discountRepository;
    }

    public List<Game> findAll() {
        return gameRepository.findAllByOrderByCreatedOnDesc();
    }

    public boolean create(CreateGameRequest createGameRequest, Category category, Company company, String imagePath) {
        Game game = new Game();
        Discount discount = new Discount();
        discount.setAmount(0);
        discount.setCreatedOn(LocalDateTime.now());

        discountRepository.save(discount);

        game.setTitle(createGameRequest.getTitle());
        game.setDescription(createGameRequest.getDescription());
        game.setImage(imagePath);
        game.setCategory(category);
        game.setCompany(company);
        game.setDiscount(discount);
        game.setPrice(createGameRequest.getPrice());
        game.setCreatedOn(LocalDateTime.now());
        game.setUpdatedOn(LocalDateTime.now());

        Optional<Game> findByName = gameRepository.findByTitle(game.getTitle());

        if (findByName.isEmpty()) {
            gameRepository.save(game);
            return true;
        } else {
            return false;
        }
    }

    public Game findById(UUID id) {
        return gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game does not exist!"));
    }

    public void edit(UUID id, CreateGameRequest createGameRequest, Category category, Company company, String imagePath) {
        Game game = findById(id);

        game.setTitle(createGameRequest.getTitle());
        game.setDescription(createGameRequest.getDescription());
        game.setImage(imagePath == null ? game.getImage() : imagePath);
        game.setCategory(category);
        game.setCompany(company);
        game.setPrice(createGameRequest.getPrice());
        game.setUpdatedOn(LocalDateTime.now());

        gameRepository.save(game);
    }

    public void deleteById(UUID id) {
        gameRepository.deleteById(id);
    }
}
