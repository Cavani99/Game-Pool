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
@RequestMapping("/dashboard/games")
public class GamesController {

    private final UserService userService;
    private final GameService gameService;

    public GamesController(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @GetMapping
    @RequestMapping
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
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "games");

        return modelAndView;
    }
}
