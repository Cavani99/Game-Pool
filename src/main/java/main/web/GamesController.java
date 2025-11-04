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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
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
}
