package main.web;

import main.model.Category;
import main.model.Company;
import main.model.Game;
import main.model.User;
import main.security.AuthenticationDetails;
import main.service.CategoryService;
import main.service.CompanyService;
import main.service.GameService;
import main.service.UserService;
import main.web.dto.GameFilterRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/games")
public class GamesController {

    private final UserService userService;
    private final GameService gameService;
    private final CompanyService companyService;
    private final CategoryService categoryService;

    public GamesController(UserService userService, GameService gameService,
                           CompanyService companyService, CategoryService categoryService) {
        this.userService = userService;
        this.gameService = gameService;
        this.companyService = companyService;
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getGamesView(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("games-list");

        User user = userService.getById(userDetails.getId());

        List<Game> boughtGames = user.getGames();

        List<Game> games = gameService.findAll();

        List<Game> availableGames = games.stream()
                .filter(game -> !boughtGames.contains(game))
                .toList();

        List<Category> categories = getCategoriesByGames(availableGames);
        List<Company> companies = getCompaniesByGames(availableGames);

        modelAndView.addObject("games", availableGames);
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("companies", companies);
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "games");

        return modelAndView;
    }

    private List<Category> getCategoriesByGames(List<Game> games) {
        List<Category> availableCategories = games.stream()
                .map(Game::getCategory)
                .distinct()
                .toList();
        List<UUID> categoryIds = availableCategories.stream()
                .map(Category::getId)
                .distinct()
                .toList();

        return categoryService.findByCategoriesList(categoryIds);
    }

    private List<Company> getCompaniesByGames(List<Game> games) {
        List<Company> availableCompanies = games.stream()
                .map(Game::getCompany)
                .distinct()
                .toList();
        List<UUID> companyIds = availableCompanies.stream()
                .map(Company::getId)
                .distinct()
                .toList();

        return companyService.findByCompaniesList(companyIds);
    }

    @PostMapping("filter")
    public ModelAndView getGamesByFilter(
            @RequestBody GameFilterRequest gameFilterRequest,
            @AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView("/fragments/game-list :: games");

        User user = userService.getById(userDetails.getId());

        List<Game> boughtGames = user.getGames();

        List<Game> filteredGames = gameService.getFilteredGames(
                gameFilterRequest.getCategories(),
                gameFilterRequest.getCompanies()
        );

        List<Game> availableGames = filteredGames.stream()
                .filter(game -> !boughtGames.contains(game))
                .toList();

        modelAndView.addObject("games", availableGames);
        return modelAndView;
    }

    @GetMapping("details/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getGameDetails(@PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game-details");

        Game game = gameService.findById(id);
        User user = userService.getById(userDetails.getId());
        boolean isWishListed = user.gameIsWishlisted(game.getId());

        modelAndView.addObject("game", game);
        modelAndView.addObject("isWishListed", isWishListed);
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "games");

        return modelAndView;
    }

    @PostMapping("wishlist")
    @ResponseBody
    public Map<String, Object> wishlistGame(@RequestBody Map<String, String> request, @AuthenticationPrincipal AuthenticationDetails userDetails) {
        UUID gameId = UUID.fromString(request.get("id"));
        Game game = gameService.findById(gameId);

        User user = userService.getById(userDetails.getId());
        userService.wishlistGame(user, game);

        boolean isWishlisted = user.gameIsWishlisted(game.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("wishlisted", isWishlisted);

        return response;
    }

    @PostMapping("buy")
    @ResponseBody
    public Map<String, Object> buyGame(@RequestBody Map<String, String> request, @AuthenticationPrincipal AuthenticationDetails userDetails) {
        UUID gameId = UUID.fromString(request.get("id"));
        Game game = gameService.findById(gameId);
        User user = userService.getById(userDetails.getId());

        Map<String, Object> response = new HashMap<>();
        if (!userService.hasFundsForGame(user, game)) {
            response.put("status", "error");
            response.put("message", "You do not have enough funds to buy this game!");

            return response;
        }

        userService.buyGame(user, game);
        response.put("status", "success");

        return response;
    }

    @GetMapping("remove_wishlist/{id}")
    @ResponseBody
    public ModelAndView wishlistGame(@PathVariable("id") UUID gameId, @AuthenticationPrincipal AuthenticationDetails userDetails) {
        Game game = gameService.findById(gameId);

        User user = userService.getById(userDetails.getId());
        userService.wishlistGame(user, game);

        return new ModelAndView("redirect:/dashboard/games/wishlist");
    }

    @GetMapping("wishlist")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getWishlist(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wishlist");

        User user = userService.getById(userDetails.getId());

        List<Game> games = user.getWishlistGames();

        List<Game> boughtGames = user.getGames();

        List<Game> availableGames = games.stream()
                .filter(game -> !boughtGames.contains(game))
                .toList();

        List<Category> categories = getCategoriesByGames(availableGames);
        List<Company> companies = getCompaniesByGames(availableGames);

        modelAndView.addObject("games", availableGames);
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("companies", companies);
        modelAndView.addObject("page", "wishlist");
        modelAndView.addObject("title", "Wish List");

        return modelAndView;
    }

    @PostMapping("filter_wishlist")
    public ModelAndView getWishlistedGamesByFilter(
            @RequestBody GameFilterRequest gameFilterRequest,
            @AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView("/fragments/wishlist-fragment :: games");

        User user = userService.getById(userDetails.getId());

        List<Game> games = user.getWishlistGames();

        List<Game> boughtGames = user.getGames();

        List<Game> filteredGames = gameService.getFilteredGames(
                gameFilterRequest.getCategories(),
                gameFilterRequest.getCompanies()
        );

        List<Game> availableGames = filteredGames.stream()
                .filter(games::contains)
                .filter(game -> !boughtGames.contains(game))
                .toList();

        modelAndView.addObject("games", availableGames);
        return modelAndView;
    }

    @GetMapping("user_games/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getUserGames(@PathVariable("id") UUID userId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user_games");

        User user = userService.getById(userId);

        List<Game> boughtGames = user.getGames();

        modelAndView.addObject("user", user);
        modelAndView.addObject("games", boughtGames);
        modelAndView.addObject("page", "home");
        modelAndView.addObject("title", "Home");

        return modelAndView;
    }
}
