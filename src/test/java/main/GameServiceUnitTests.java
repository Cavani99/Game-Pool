package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.model.*;
import project.repository.DiscountRepository;
import project.repository.GameRepository;
import project.service.GameService;
import project.web.dto.CreateGameRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceUnitTests {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    public void gameFindAllIsSortedRight() {
        Game game1 = new Game();
        game1.setId(UUID.randomUUID());
        game1.setCreatedOn(LocalDateTime.now());

        Game game2 = new Game();
        game2.setId(UUID.randomUUID());
        game2.setCreatedOn(LocalDateTime.now().plusHours(5));

        when(gameRepository.findAllByOrderByCreatedOnDesc())
                .thenReturn(List.of(game2, game1));

        List<Game> result = gameService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedOn()
                .isAfter(result.get(1).getCreatedOn()));

        verify(gameRepository).findAllByOrderByCreatedOnDesc();
    }

    @Test
    public void whenGameNamesIsNew_thenReturnTrueOnCreate() {
        CreateGameRequest gameRequest = CreateGameRequest.builder()
                .title("Game")
                .price(15.0)
                .description("Test Game")
                .build();

        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Cat");

        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Comp");

        when(gameRepository.findByTitle("Game"))
                .thenReturn(Optional.empty());

        boolean result = gameService.create(gameRequest, category, company, "/uploads/games/img.jpg");

        assertTrue(result);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    public void whenGameNameExists_thenReturnFalseOnCreate() {
        CreateGameRequest gameRequest = CreateGameRequest.builder()
                .title("Game")
                .price(15.0)
                .description("Test Game")
                .build();

        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Cat");

        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Comp");

        when(gameRepository.findByTitle("Game"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Game()));

        boolean firstResult = gameService.create(gameRequest, category, company, "/uploads/games/img.jpg");
        boolean secondResult = gameService.create(gameRequest, category, company, "/uploads/games/img.jpg");

        assertTrue(firstResult);
        assertFalse(secondResult);
    }

    @Test
    public void whenGameIdExistReturnGame() {
        UUID gameId = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);
        game.setTitle("Game");

        when(gameRepository.findById(gameId))
                .thenReturn(Optional.of(game));

        Game result = gameService.findById(gameId);

        assertNotNull(result);
        assertEquals(gameId, result.getId());
        assertEquals("Game", result.getTitle());
        verify(gameRepository).findById(gameId);
    }

    @Test
    public void whenGameIdNotExistThrowRuntimeException() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findById(any()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> gameService.findById(gameId)
        );

        assertEquals("Game does not exist!", exception.getMessage());
    }

    @Test
    public void whenGameEdited_verifyChanges() {
        Game game = new Game();
        UUID gameId = UUID.randomUUID();
        CreateGameRequest gameRequest = CreateGameRequest.builder()
                .title("Game")
                .price(15.0)
                .description("Test Game")
                .build();


        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Cat");

        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Comp");

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        gameService.edit(gameId, gameRequest, category, company, "/uploads/games/img.jpg");

        assertEquals("Game", game.getTitle());
        assertEquals(15.0, game.getPrice());
        assertEquals("Test Game", game.getDescription());
        assertEquals("/uploads/games/img.jpg", game.getImage());
        assertEquals(category, game.getCategory());
        assertEquals(company, game.getCompany());
        assertNotNull(game.getUpdatedOn());

        verify(gameRepository).findById(gameId);
        verify(gameRepository).save(game);
    }

    @Test
    public void whenEditGameHasNoImage_thenGetCurrentImage() {
        Game game = new Game();
        game.setImage("/uploads/games/non.jpg");
        UUID gameId = UUID.randomUUID();
        CreateGameRequest gameRequest = CreateGameRequest.builder()
                .title("Game")
                .price(15.0)
                .description("Test Game")
                .build();


        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Cat");

        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Comp");

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        gameService.edit(gameId, gameRequest, category, company, null);

        assertEquals("Game", game.getTitle());
        assertEquals(15.0, game.getPrice());
        assertEquals("Test Game", game.getDescription());
        assertEquals("/uploads/games/non.jpg", game.getImage());
        assertEquals(category, game.getCategory());
        assertEquals(company, game.getCompany());
        assertNotNull(game.getUpdatedOn());

        verify(gameRepository).findById(gameId);
        verify(gameRepository).save(game);
    }

    @Test
    public void whenGameExists_thenDeleteById() {
        UUID gameId = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);
        game.setTitle("Game");

        gameService.deleteById(gameId);

        verify(gameRepository).deleteById(gameId);
    }

    @Test
    public void whenFilteringGames_onlyMatchingOnesAreReturned() {
        Category mockCategory = new Category();
        mockCategory.setId(UUID.randomUUID());

        Category nonMatchCategory = new Category();
        nonMatchCategory.setId(UUID.randomUUID());

        Company mockCompany = new Company();
        mockCompany.setId(UUID.randomUUID());

        Company nonMatchCompany = new Company();
        nonMatchCompany.setId(UUID.randomUUID());

        Game matchingGame = new Game();
        matchingGame.setId(UUID.randomUUID());
        matchingGame.setCategory(mockCategory);
        matchingGame.setCompany(mockCompany);

        Game nonMatchingGame = new Game();
        nonMatchingGame.setId(UUID.randomUUID());
        nonMatchingGame.setCategory(nonMatchCategory);
        nonMatchingGame.setCompany(nonMatchCompany);

        when(gameRepository.findAllByCategoryIdOrCompanyIdList(
                anyList(), anyList()
        )).thenReturn(List.of(matchingGame));

        List<UUID> categoryFilter = List.of(mockCategory.getId());
        List<UUID> companyFilter = List.of(mockCompany.getId());

        List<Game> result =
                gameService.getFilteredGames(categoryFilter, companyFilter);


        assertEquals(1, result.size());
        assertTrue(result.contains(matchingGame));
        assertFalse(result.contains(nonMatchingGame));

        verify(gameRepository).findAllByCategoryIdOrCompanyIdList(categoryFilter, companyFilter);
    }

    @Test
    public void whenCategoriesAndCompaniesEmpty_thenReturnAllGames() {
        Game matchingGame = new Game();
        matchingGame.setId(UUID.randomUUID());
        matchingGame.setCreatedOn(LocalDateTime.now());

        Game nonMatchingGame = new Game();
        nonMatchingGame.setId(UUID.randomUUID());
        nonMatchingGame.setCreatedOn(LocalDateTime.now());

        List<Game> allGames = List.of(matchingGame, nonMatchingGame);

        when(gameRepository.findAllByOrderByCreatedOnDesc()).thenReturn(allGames);

        List<UUID> categoryFilter = List.of();
        List<UUID> companyFilter;

        List<Game> result = gameService.getFilteredGames(categoryFilter, null);

        assertEquals(2, result.size());
        assertTrue(result.contains(matchingGame));
        assertTrue(result.contains(nonMatchingGame));

        verify(gameRepository).findAllByOrderByCreatedOnDesc();
        verify(gameRepository, never()).findAllByCategoryIdOrCompanyIdList(anyList(), anyList());

        reset(gameRepository);
        when(gameRepository.findAllByOrderByCreatedOnDesc()).thenReturn(allGames);

        companyFilter = List.of();

        List<Game> result2 = gameService.getFilteredGames(null, companyFilter);

        assertEquals(2, result2.size());
        assertTrue(result2.contains(matchingGame));
        assertTrue(result2.contains(nonMatchingGame));

        verify(gameRepository).findAllByOrderByCreatedOnDesc();
        verify(gameRepository, never()).findAllByCategoryIdOrCompanyIdList(anyList(), anyList());

        reset(gameRepository);
        categoryFilter = List.of();
        companyFilter = List.of(UUID.randomUUID());

        gameService.getFilteredGames(categoryFilter, companyFilter);

        verify(gameRepository, never()).findAll();
        verify(gameRepository).findAllByCategoryIdOrCompanyIdList(categoryFilter, companyFilter);
    }

    @Test
    public void whenDiscountIsAdded_thenMatchTheDiscount() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(5);

        Discount discount = new Discount();
        discount.setType(DiscountType.PERCENT);
        discount.setAmount(15);
        discount.setStartDate(start);
        discount.setEndDate(end);

        UUID gameId = UUID.randomUUID();
        Game game = new Game();
        game.setId(gameId);
        game.setTitle("Game");
        game.setDiscount(discount);


        when(gameRepository.save(game)).thenReturn(game);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Game result = gameService.addDiscount(gameId, discount);

        assertNotNull(result);
        assertEquals(gameId, result.getId());
        assertEquals("Game", result.getTitle());
        assertEquals(15, result.getDiscount().getAmount());
        assertEquals(DiscountType.PERCENT, result.getDiscount().getType());
        assertEquals(start, result.getDiscount().getStartDate());
        assertEquals(end, result.getDiscount().getEndDate());
        verify(gameRepository).save(game);
    }

    @Test
    public void whenYouGetPromoPrice_thenReturnPromoPriceElseGetPrice() {
        Discount discount = new Discount();
        discount.setType(DiscountType.FIXED);
        discount.setAmount(15);
        discount.setEndDate(LocalDateTime.now().plusDays(10));

        Game game = new Game();
        game.setPrice(45.00);
        game.setDiscount(discount);

        double price = gameService.getActualPrice(game);

        assertEquals(30, price);

        Discount discount2 = new Discount();
        discount2.setType(DiscountType.PERCENT);
        discount2.setAmount(10);
        discount2.setEndDate(LocalDateTime.now().plusDays(10));

        Game game2 = new Game();
        game2.setPrice(100.00);
        game2.setDiscount(discount2);

        double price2 = gameService.getActualPrice(game2);
        assertEquals(90, price2);

        Discount discount3 = new Discount();
        discount3.setType(DiscountType.FIXED);
        discount3.setAmount(10);
        discount3.setEndDate(LocalDateTime.now().minusDays(2));

        Game game3 = new Game();
        game3.setPrice(50.00);
        game3.setDiscount(discount3);

        double price3 = gameService.getActualPrice(game3);
        assertEquals(50, price3);
    }

}
