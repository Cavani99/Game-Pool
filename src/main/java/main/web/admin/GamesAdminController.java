package main.web.admin;

import jakarta.validation.Valid;
import main.model.Category;
import main.model.Company;
import main.model.Game;
import main.service.CategoryService;
import main.service.CompanyService;
import main.service.GameService;
import main.web.dto.CreateGameRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/games")
public class GamesAdminController {

    private final GameService gameService;
    private final CategoryService categoryService;
    private final CompanyService companyService;

    public GamesAdminController(GameService gameService, CategoryService categoryService, CompanyService companyService) {
        this.gameService = gameService;
        this.categoryService = categoryService;
        this.companyService = companyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView getGames() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/games");

        List<Game> games = gameService.findAll();

        modelAndView.addObject("games", games);
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "Games");

        return modelAndView;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createGame() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/game_form");
        modelAndView.addObject("game", new CreateGameRequest());
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "Games");

        List<Category> categories = categoryService.findAll();
        List<Company> companies = companyService.findAll();

        modelAndView.addObject("categories", categories);
        modelAndView.addObject("companies", companies);

        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createGame(@Valid @ModelAttribute("game") CreateGameRequest createGameRequest, BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) throws IOException {
        List<Category> categories = categoryService.findAll();
        List<Company> companies = companyService.findAll();

        if (createGameRequest.getImage() == null || createGameRequest.getImage().isEmpty()) {
            bindingResult.rejectValue("image", "image.empty", "Please, pick an image");
        }

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/game_form");
            mav.addObject("game", createGameRequest);
            mav.addObject("page", "games");
            mav.addObject("title", "Games");
            mav.addObject("categories", categories);
            mav.addObject("companies", companies);
            return mav;
        }

        //save image and get path
        MultipartFile image = createGameRequest.getImage();
        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/games/";
            Files.createDirectories(Paths.get(uploadDir));

            String originalName = image.getOriginalFilename();
            String latinName = Normalizer.normalize(originalName, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[^a-zA-Z0-9._-]", "_");

            String filename = UUID.randomUUID() + "_" + latinName;
            Path filePath = Paths.get(uploadDir + filename);
            Files.write(filePath, image.getBytes());

            imagePath = "/uploads/games/" + filename;
        }
        Category category = categoryService.findById(createGameRequest.getCategoryId());
        Company company = companyService.findById(createGameRequest.getCompanyId());


        if (gameService.create(createGameRequest, category, company, imagePath)) {

            redirectAttributes.addFlashAttribute("message", "Game " + createGameRequest.getTitle() + " created successfully!");

            return new ModelAndView("redirect:/admin/games");
        } else {
            bindingResult.rejectValue("title", "error.game", "A Game with this name already exists.");
            ModelAndView mav = new ModelAndView("admin/game_form");
            mav.addObject("game", createGameRequest);
            mav.addObject("page", "games");
            mav.addObject("title", "Games");
            mav.addObject("categories", categories);
            mav.addObject("companies", companies);
            return mav;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editGame(@PathVariable("id") UUID id) {
        Game game = gameService.findById(id);
        CreateGameRequest createGameRequest = new CreateGameRequest(game.getTitle(), game.getDescription(), null,
                game.getCategory().getId(), game.getCompany().getId(), game.getImage());

        List<Category> categories = categoryService.findAll();
        List<Company> companies = companyService.findAll();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/game_form");
        modelAndView.addObject("game", createGameRequest);
        modelAndView.addObject("game_id", game.getId());
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "Games");
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("companies", companies);

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editGame(@PathVariable("id") UUID id, @Valid @ModelAttribute("game") CreateGameRequest createGameRequest,
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes) throws IOException {
        List<Category> categories = categoryService.findAll();
        List<Company> companies = companyService.findAll();
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/game_form");
            mav.addObject("game", createGameRequest);
            mav.addObject("page", "games");
            mav.addObject("title", "Games");
            mav.addObject("categories", categories);
            mav.addObject("companies", companies);
            return mav;
        }

        MultipartFile image = createGameRequest.getImage();
        String imagePath =null;
        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/games/";
            Files.createDirectories(Paths.get(uploadDir));

            String originalName = image.getOriginalFilename();
            String latinName = Normalizer.normalize(originalName, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[^a-zA-Z0-9._-]", "_");

            String filename = UUID.randomUUID() + "_" + latinName;
            Path filePath = Paths.get(uploadDir + filename);
            Files.write(filePath, image.getBytes());

            imagePath = "/uploads/games/" + filename;
        }

        Category category = categoryService.findById(createGameRequest.getCategoryId());
        Company company = companyService.findById(createGameRequest.getCompanyId());

        gameService.edit(id, createGameRequest, category, company, imagePath);

        redirectAttributes.addFlashAttribute("message", "Game " + createGameRequest.getTitle() + " saved successfully!");

        return new ModelAndView("redirect:/admin/games");
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView deleteGame(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        gameService.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "Game deleted!");

        return new ModelAndView("redirect:/admin/games");
    }
}
