package main.web;

import main.model.Game;
import main.model.User;
import main.security.AuthenticationDetails;
import main.service.GameService;
import main.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final GameService gameService;

    public DashboardController(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getHomepage(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping
    @RequestMapping("/games")
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

        modelAndView.addObject("games", availableGames);

        return modelAndView;
    }
}
