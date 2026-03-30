package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import project.model.*;
import project.security.AuthenticationDetails;
import project.service.*;
import project.web.GamesController;
import project.web.dto.GameFilterRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamesControllerUnitTests {

    @Mock
    UserService userService;
    @Mock
    GameService gameService;
    @Mock
    CompanyService companyService;
    @Mock
    CategoryService categoryService;
    @Mock
    TransactionService transactionService;

    @InjectMocks
    GamesController controller;

    @Test
    void testGetGamesView() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setGames(List.of());

        Game g = new Game();
        g.setId(UUID.randomUUID());
        g.setCategory(new Category(UUID.randomUUID(), "Action", LocalDateTime.now(), LocalDateTime.now(),
                new ArrayList<>()));
        g.setCompany(new Company(UUID.randomUUID(), "Ubisoft", LocalDateTime.now(), LocalDateTime.now(),
                new ArrayList<>()));

        when(userService.getById(details.getId())).thenReturn(user);
        when(gameService.findAll()).thenReturn(List.of(g));
        when(categoryService.findByCategoriesList(any())).thenReturn(List.of(g.getCategory()));
        when(companyService.findByCompaniesList(any())).thenReturn(List.of(g.getCompany()));

        ModelAndView mav = controller.getGamesView(details);

        assertEquals("games-list", mav.getViewName());
        assertEquals(List.of(g), mav.getModel().get("games"));
    }

    @Test
    void testGetGamesView_OwnedGameFilteredOut() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        Game ownedGame = new Game();
        ownedGame.setId(UUID.randomUUID());
        ownedGame.setCategory(new Category(UUID.randomUUID(), "Action", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>()));
        ownedGame.setCompany(new Company(UUID.randomUUID(), "Ubisoft", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>()));

        User user = new User();
        user.setId(details.getId());
        user.setGames(List.of(ownedGame));

        when(userService.getById(details.getId())).thenReturn(user);
        when(gameService.findAll()).thenReturn(List.of(ownedGame));
        when(categoryService.findByCategoriesList(any())).thenReturn(List.of(ownedGame.getCategory()));
        when(companyService.findByCompaniesList(any())).thenReturn(List.of(ownedGame.getCompany()));

        ModelAndView mav = controller.getGamesView(details);

        assertEquals("games-list", mav.getViewName());

        List<Game> games = (List<Game>) mav.getModel().get("games");
        assertEquals(0, games.size());
    }

    @Test
    void testWishlistGame() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        UUID gid = UUID.randomUUID();

        Game game = new Game();
        game.setId(gid);
        game.setTitle("Halo");

        User user = new User();
        user.setId(details.getId());

        when(gameService.findById(gid)).thenReturn(game);
        when(userService.getById(details.getId())).thenReturn(user);

        Map<String, String> req = Map.of("id", gid.toString());

        Locale locale = Locale.getDefault();
        Map<String, Object> res = controller.wishlistGame(req, details, locale);

        verify(userService).wishlistGame(user, game);
    }

    @Test
    void testBuyGame_WithEnoughFunds() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        UUID gid = UUID.randomUUID();

        Game game = new Game();
        game.setId(gid);
        game.setTitle("Halo");

        User user = new User();
        user.setId(details.getId());

        when(gameService.findById(gid)).thenReturn(game);
        when(userService.getById(details.getId())).thenReturn(user);
        when(userService.hasFundsForGame(user, game)).thenReturn(true);

        Locale locale = Locale.getDefault();
        Map<String, Object> res = controller.buyGame(Map.of("id", gid.toString()), details, locale);

        verify(userService).buyGame(user, game);
        verify(transactionService).createBuyGameTransaction(user.getId(), game);
        assertEquals("success", res.get("status"));
    }

    @Test
    void testBuyGame_NotEnoughFunds() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        UUID gid = UUID.randomUUID();

        Game game = new Game();
        game.setId(gid);
        game.setTitle("Halo");

        User user = new User();
        user.setId(details.getId());

        when(gameService.findById(gid)).thenReturn(game);
        when(userService.getById(details.getId())).thenReturn(user);
        when(userService.hasFundsForGame(user, game)).thenReturn(false);

        Locale locale = Locale.getDefault();
        Map<String, Object> res = controller.buyGame(Map.of("id", gid.toString()), details, locale);

        assertEquals("error", res.get("status"));
    }

    @Test
    void getGamesByFilter_ShouldReturnFilteredAvailableGames() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User user = new User();
        user.setId(details.getId());
        Game bought = new Game();
        bought.setId(UUID.randomUUID());

        Game g1 = new Game();
        g1.setId(UUID.randomUUID());
        Game g2 = new Game();
        g2.setId(UUID.randomUUID());

        user.setGames(List.of(bought));

        when(userService.getById(details.getId())).thenReturn(user);

        GameFilterRequest req = new GameFilterRequest();
        req.setCategories(List.of(UUID.randomUUID()));
        req.setCompanies(List.of(UUID.randomUUID()));

        when(gameService.getFilteredGames(req.getCategories(), req.getCompanies()))
                .thenReturn(List.of(bought, g1, g2));

        ModelAndView mav = controller.getGamesByFilter(req, details);

        assertEquals("/fragments/game-list :: games", mav.getViewName());

        List<Game> games = (List<Game>) mav.getModel().get("games");
        assertEquals(List.of(g1, g2), games);
    }

    @Test
    void getGameDetails_ShouldReturnCorrectModel() {
        UUID gameId = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);

        UUID userId = UUID.randomUUID();
        AuthenticationDetails auth = mock(AuthenticationDetails.class);
        when(auth.getId()).thenReturn(userId);

        User user = mock(User.class);
        when(userService.getById(userId)).thenReturn(user);
        when(gameService.findById(gameId)).thenReturn(game);
        when(user.gameIsWishlisted(gameId)).thenReturn(true);

        ModelAndView mav = controller.getGameDetails(gameId, auth);

        assertEquals("game-details", mav.getViewName());
        assertEquals(game, mav.getModel().get("game"));
        assertEquals(true, mav.getModel().get("isWishListed"));
    }

    @Test
    void wishlistGame_ShouldCallServiceAndRedirect() {
        UUID gameId = UUID.randomUUID();

        AuthenticationDetails auth = mock(AuthenticationDetails.class);
        UUID userId = UUID.randomUUID();
        when(auth.getId()).thenReturn(userId);

        User user = new User();
        Game game = new Game();

        when(userService.getById(userId)).thenReturn(user);
        when(gameService.findById(gameId)).thenReturn(game);

        Locale locale = Locale.getDefault();
        ModelAndView mav = controller.wishlistGame(gameId, auth, locale);

        verify(userService).wishlistGame(user, game);

        assertEquals("redirect:/dashboard/games/wishlist", mav.getViewName());
    }

    @Test
    void getWishlist_ShouldReturnAvailableGames() {
        AuthenticationDetails auth = mock(AuthenticationDetails.class);
        UUID userId = UUID.randomUUID();
        when(auth.getId()).thenReturn(userId);

        Game owned = new Game();
        owned.setId(UUID.randomUUID());

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Company company = new Company();
        company.setId(UUID.randomUUID());

        Game wish1 = new Game();
        wish1.setId(UUID.randomUUID());
        wish1.setCategory(category);
        wish1.setCompany(company);

        Game wish2 = new Game();
        wish2.setId(UUID.randomUUID());
        wish2.setCategory(category);
        wish2.setCompany(company);

        User user = mock(User.class);
        when(user.getGames()).thenReturn(List.of(owned));
        when(user.getWishlistGames()).thenReturn(List.of(owned, wish1, wish2));
        when(userService.getById(userId)).thenReturn(user);

        ModelAndView mav = controller.getWishlist(auth);

        assertEquals("wishlist", mav.getViewName());

        List<Game> games = (List<Game>) mav.getModel().get("games");
        assertEquals(List.of(wish1, wish2), games);
    }

    @Test
    void getWishlistedGamesByFilter_ShouldReturnAvailableWishlistedGames() {
        AuthenticationDetails auth = mock(AuthenticationDetails.class);
        UUID userId = UUID.randomUUID();
        when(auth.getId()).thenReturn(userId);

        Game owned = new Game();
        owned.setId(UUID.randomUUID());

        Game wish1 = new Game();
        wish1.setId(UUID.randomUUID());

        Game wish2 = new Game();
        wish2.setId(UUID.randomUUID());

        User user = mock(User.class);
        when(user.getGames()).thenReturn(List.of(owned));
        when(user.getWishlistGames()).thenReturn(List.of(wish1, wish2));
        when(userService.getById(userId)).thenReturn(user);

        GameFilterRequest req = new GameFilterRequest();
        req.setCategories(List.of());
        req.setCompanies(List.of());

        when(gameService.getFilteredGames(any(), any()))
                .thenReturn(List.of(wish1, wish2, owned)); // owned excluded

        ModelAndView mav = controller.getWishlistedGamesByFilter(req, auth);

        assertEquals("/fragments/wishlist-fragment :: games", mav.getViewName());

        List<Game> games = (List<Game>) mav.getModel().get("games");
        assertEquals(List.of(wish1, wish2), games);
    }

    @Test
    void whenWishlistedGamesByFilterWithOwnedGame_thenFilterOutOwnedGame() {
        AuthenticationDetails auth = mock(AuthenticationDetails.class);
        UUID userId = UUID.randomUUID();
        when(auth.getId()).thenReturn(userId);

        Game owned = new Game();
        owned.setId(UUID.randomUUID());

        Game wish1 = new Game();
        wish1.setId(UUID.randomUUID());

        Game wish2 = new Game();
        wish2.setId(owned.getId());

        User user = mock(User.class);
        when(user.getGames()).thenReturn(List.of(owned));
        when(user.getWishlistGames()).thenReturn(List.of(wish1, wish2));
        when(userService.getById(userId)).thenReturn(user);

        GameFilterRequest req = new GameFilterRequest();
        req.setCategories(List.of());
        req.setCompanies(List.of());

        when(gameService.getFilteredGames(any(), any()))
                .thenReturn(List.of(wish1, wish2));

        ModelAndView mav = controller.getWishlistedGamesByFilter(req, auth);

        assertEquals("/fragments/wishlist-fragment :: games", mav.getViewName());

        List<Game> games = (List<Game>) mav.getModel().get("games");

        assertEquals(List.of(wish1), games);
    }

    @Test
    void getUserGames_ShouldReturnUserAndGames() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        Game g1 = new Game();
        Game g2 = new Game();
        user.setGames(List.of(g1, g2));

        when(userService.getById(userId)).thenReturn(user);

        ModelAndView mav = controller.getUserGames(userId);

        assertEquals("user_games", mav.getViewName());
        assertEquals(user, mav.getModel().get("user"));
        assertEquals(List.of(g1, g2), mav.getModel().get("games"));
    }

}

