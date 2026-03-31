package project.web.admin;

import jakarta.validation.Valid;
import project.model.*;
import project.service.*;
import project.utils.ImagesCleanupService;
import project.web.dto.CreateDiscountRequest;
import project.web.dto.CreateGameRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/games")
public class GamesAdminController {

    private final GameService gameService;
    private final DiscountService discountService;
    private final CategoryService categoryService;
    private final CompanyService companyService;
    private final Logger logger;

    private final ImagesCleanupService imagesCleanupService;
    private final MessageService messageService;

    @Autowired
    public GamesAdminController(GameService gameService, DiscountService discountService, CategoryService categoryService,
                                CompanyService companyService, ImagesCleanupService imagesCleanupService, MessageService messageService) {
        this.gameService = gameService;
        this.discountService = discountService;
        this.categoryService = categoryService;
        this.companyService = companyService;
        this.imagesCleanupService = imagesCleanupService;
        this.messageService = messageService;
        this.logger = LoggerFactory.getLogger(GamesAdminController.class);
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
    public ModelAndView createGame(Locale locale) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/game_form");
        modelAndView.addObject("game", new CreateGameRequest());
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "Games");

        List<Category> categories = categoryService.findAll();
        List<Company> companies = companyService.findAll();

        modelAndView.addObject("categories", categories);
        modelAndView.addObject("companies", companies);

        String message = messageService.getLocalizedMessage("form_games", locale);
        logger.info(message);

        return modelAndView;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createGame(@Valid @ModelAttribute("game") CreateGameRequest createGameRequest, BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes, Locale locale) throws IOException {
        List<Category> categories = categoryService.findAll();
        List<Company> companies = companyService.findAll();

        if (createGameRequest.getImage() == null || createGameRequest.getImage().isEmpty()) {
            String message = messageService.getLocalizedMessage("pick_image", locale);
            bindingResult.rejectValue("image", "image.empty", message);
        }

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/game_form");
            mav.addObject("game", createGameRequest);
            mav.addObject("page", "games");
            mav.addObject("title", "Games");
            mav.addObject("categories", categories);
            mav.addObject("companies", companies);

            String message = messageService.getLocalizedMessage("errors_create_game", locale);
            logger.error(message, bindingResult.getAllErrors());

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

            String message = messageService.getLocalizedMessage("game_name_created", locale);
            message = message.replace("{}", createGameRequest.getTitle());
            redirectAttributes.addFlashAttribute("message", message);

            message = messageService.getLocalizedMessage("game_created", locale);
            logger.info(message, createGameRequest.getTitle());

            return new ModelAndView("redirect:/admin/games");
        } else {
            String message = messageService.getLocalizedMessage("game_exists", locale);
            bindingResult.rejectValue("title", "error.game", message);
            ModelAndView mav = new ModelAndView("admin/game_form");
            mav.addObject("game", createGameRequest);
            mav.addObject("page", "games");
            mav.addObject("title", "Games");
            mav.addObject("categories", categories);
            mav.addObject("companies", companies);

            message = messageService.getLocalizedMessage("errors_create_game", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editGame(@PathVariable("id") UUID id, Locale locale) {
        Game game = gameService.findById(id);
        CreateGameRequest createGameRequest = new CreateGameRequest(game.getTitle(), game.getDescription(), null,
                game.getCategory().getId(), game.getCompany().getId(), game.getImage(), game.getPrice());

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

        String message = messageService.getLocalizedMessage("game_edit", locale);
        logger.info(message, game.getTitle());

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editGame(@PathVariable("id") UUID id, @Valid @ModelAttribute("game") CreateGameRequest createGameRequest,
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes, Locale locale) throws IOException {
        List<Category> categories = categoryService.findAll();
        List<Company> companies = companyService.findAll();

        if (bindingResult.hasErrors()) {
            Game game = gameService.findById(id);
            createGameRequest.setImagePath(game.getImage());

            ModelAndView mav = new ModelAndView("admin/game_form");
            mav.addObject("game", createGameRequest);
            mav.addObject("game_id", game.getId());
            mav.addObject("page", "games");
            mav.addObject("title", "Games");
            mav.addObject("categories", categories);
            mav.addObject("companies", companies);

            String message = messageService.getLocalizedMessage("errors_edit_game", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }

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

        gameService.edit(id, createGameRequest, category, company, imagePath);

        String message = messageService.getLocalizedMessage("game_saved", locale);
        message = message.replace("{}", createGameRequest.getTitle());
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("game_edited", locale);
        logger.info(message, createGameRequest.getTitle());

        return new ModelAndView("redirect:/admin/games");
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView deleteGame(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes, Locale locale) {
        gameService.deleteById(id);

        String message = messageService.getLocalizedMessage("game_deleted", locale);
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("game_id_deleted", locale);
        logger.info(message, id);

        return new ModelAndView("redirect:/admin/games");
    }

    @GetMapping("/discount/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView addDiscount(@PathVariable("id") UUID id, Locale locale) {
        Game game = gameService.findById(id);
        Discount discount = game.getDiscount();

        CreateDiscountRequest createDiscountRequest;
        if (discount != null) {
            createDiscountRequest = new CreateDiscountRequest(
                    discount.getAmount(), discount.getType(), discount.getStartDate(), discount.getEndDate()
            );
        } else {
            createDiscountRequest = new CreateDiscountRequest();
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/discount_form");
        modelAndView.addObject("discount", createDiscountRequest);
        modelAndView.addObject("game_id", id);
        modelAndView.addObject("page", "games");
        modelAndView.addObject("title", "Games");

        String message = messageService.getLocalizedMessage("game_add_discount", locale);
        logger.info(message, game.getTitle());

        return modelAndView;
    }

    @PostMapping("/discount/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView addDiscount(@PathVariable("id") UUID id, @Valid @ModelAttribute("discount") CreateDiscountRequest createDiscountRequest,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes, Locale locale) {

        Game game = gameService.findById(id);
        setManualValidations(bindingResult, createDiscountRequest, locale);
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("admin/discount_form");
            mav.addObject("discount", createDiscountRequest);
            mav.addObject("game_id", id);
            mav.addObject("page", "games");
            mav.addObject("title", "Games");

            String message = messageService.getLocalizedMessage("game_add_discount_error", locale);
            logger.error(message, game.getTitle(), bindingResult.getAllErrors());

            return mav;
        }
        Discount discount = game.getDiscount();

        discount = discountService.persist(discount, createDiscountRequest);
        game = gameService.addDiscount(id, discount);

        String message = messageService.getLocalizedMessage("game_discount_saved", locale);
        message = message.replace("{}", game.getTitle());
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("game_discount_added", locale);
        logger.info(message, game.getTitle());

        return new ModelAndView("redirect:/admin/games");
    }

    private void setManualValidations(BindingResult bindingResult, CreateDiscountRequest createDiscountRequest, Locale locale) {
        if (createDiscountRequest.getAmount() <= 0) {
            String message = messageService.getLocalizedMessage("game_discount_more_than_zero", locale);
            bindingResult.rejectValue("amount", "amount.empty", message);
        } else if (createDiscountRequest.getAmount() > 100 && createDiscountRequest.getType().equals(DiscountType.PERCENT)) {
            String message = messageService.getLocalizedMessage("game_discount_percentage_error", locale);
            bindingResult.rejectValue("amount", "amount.empty", message);
        }

        if (createDiscountRequest.getStartDate() != null && createDiscountRequest.getEndDate() != null &&
                createDiscountRequest.getStartDate().isAfter(createDiscountRequest.getEndDate())) {
            String message = messageService.getLocalizedMessage("game_discount_dates_error", locale);
            bindingResult.rejectValue("startDate", "startDate.empty", message);
        }
    }

    @GetMapping("/remove_discount/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView removeDiscount(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes, Locale locale) {
        Game game = gameService.findById(id);
        Discount discount = game.getDiscount();

        discountService.unsetDiscount(discount);

        String message = messageService.getLocalizedMessage("game_discount_removed", locale);
        message = message.replace("{}", game.getTitle());
        redirectAttributes.addFlashAttribute("message", message);

        message = messageService.getLocalizedMessage("game_discount_removed_log", locale);
        logger.info(message, game.getTitle());

        return new ModelAndView("redirect:/admin/games");
    }

    @PostMapping("/delete-images")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseBody
    public Map<String, String> removeImages(Locale locale) {
        imagesCleanupService.deleteUnusedGameImages(logger, locale);

        String message = messageService.getLocalizedMessage("games_images_deleted", locale);
        return Map.of("message", message);
    }
}
