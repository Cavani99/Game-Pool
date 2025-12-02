package project.service;

import project.exception.UnknownElementException;
import project.model.*;
import project.repository.DiscountRepository;
import project.repository.GameRepository;
import project.web.dto.CreateGameRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable("games")
    public List<Game> findAll() {
        return gameRepository.findAllByOrderByCreatedOnDesc();
    }

    @CacheEvict(value = "games", allEntries = true)
    public boolean create(CreateGameRequest createGameRequest, Category category, Company company, String imagePath) {
        Game game = new Game();
        Discount discount = new Discount();
        discount.setAmount(0);
        discount.setType(DiscountType.FIXED);
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
        return gameRepository.findById(id).orElseThrow(() -> new UnknownElementException("Game does not exist!"));
    }

    @CacheEvict(value = "games", allEntries = true)
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

    public List<Game> getFilteredGames(List<UUID> categories, List<UUID> companies) {

        if ((categories == null || categories.isEmpty()) &&
                (companies == null || companies.isEmpty())) {
            return findAll();
        }

        return gameRepository.findAllByCategoryIdOrCompanyIdList(categories, companies);
    }

    public Game addDiscount(UUID id, Discount discount) {
        Game game = findById(id);
        game.setDiscount(discount);
        game.setUpdatedOn(LocalDateTime.now());

        return gameRepository.save(game);
    }

    public double getActualPrice(Game game) {
        return game.getPromoPrice() > 0 ? game.getPromoPrice() : game.getPrice();
    }
}
