package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.model.*;
import project.service.*;
import project.web.admin.GamesAdminController;
import project.web.dto.CreateDiscountRequest;
import project.web.dto.CreateGameRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamesAdminControllerUnitTests {

    @Mock
    private GameService gameService;
    @Mock
    private DiscountService discountService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private CompanyService companyService;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private GamesAdminController controller;

    @Test
    void getGames_ShouldReturnModelAndView() {
        List<Game> list = List.of(new Game());
        when(gameService.findAll()).thenReturn(list);

        ModelAndView mav = controller.getGames();

        assertEquals("admin/games", mav.getViewName());
        assertEquals(list, mav.getModel().get("games"));
    }

    @Test
    void getAddGameForm_ShouldReturnModelAndView() {
        when(categoryService.findAll()).thenReturn(List.of());
        when(companyService.findAll()).thenReturn(List.of());

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createGame(locale);

        assertEquals("admin/game_form", mav.getViewName());
        assertInstanceOf(CreateGameRequest.class, mav.getModel().get("game"));
    }

    @Test
    void createGame_WithValidationErrors_ShouldReturnForm() throws Exception {
        CreateGameRequest req = new CreateGameRequest();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        when(categoryService.findAll()).thenReturn(List.of());
        when(companyService.findAll()).thenReturn(List.of());

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createGame(req, result, mock(RedirectAttributes.class), locale);

        assertEquals("admin/game_form", mav.getViewName());
    }

    @Test
    void createGame_Success_ShouldRedirect() throws Exception {
        CreateGameRequest req = new CreateGameRequest();
        req.setTitle("Test");
        req.setCategoryId(UUID.randomUUID());
        req.setCompanyId(UUID.randomUUID());

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("my-image.png");
        when(mockFile.getBytes()).thenReturn("dummy".getBytes());
        req.setImage(mockFile);
        when(req.getImage().isEmpty()).thenReturn(false);

        Category cat = new Category();
        Company comp = new Company();

        when(categoryService.findById(req.getCategoryId())).thenReturn(cat);
        when(companyService.findById(req.getCompanyId())).thenReturn(comp);
        when(gameService.create(any(), any(), any(), anyString())).thenReturn(true);

        when(categoryService.findAll()).thenReturn(List.of(cat));
        when(companyService.findAll()).thenReturn(List.of(comp));

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createGame(req, mock(BindingResult.class), mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/games", mav.getViewName());
    }

    @Test
    void createGame_Fail_ShouldRedirectWithErrorsToForm() throws Exception {
        CreateGameRequest req = new CreateGameRequest();
        req.setTitle("Test");
        req.setCategoryId(UUID.randomUUID());
        req.setCompanyId(UUID.randomUUID());

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("my-image.png");
        when(mockFile.getBytes()).thenReturn("dummy".getBytes());
        req.setImage(mockFile);
        when(req.getImage().isEmpty()).thenReturn(false);

        Category cat = new Category();
        Company comp = new Company();

        when(categoryService.findById(req.getCategoryId())).thenReturn(cat);
        when(companyService.findById(req.getCompanyId())).thenReturn(comp);
        when(gameService.create(any(), any(), any(), anyString())).thenReturn(false);

        when(categoryService.findAll()).thenReturn(List.of(cat));
        when(companyService.findAll()).thenReturn(List.of(comp));

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.createGame(req, mock(BindingResult.class), mock(RedirectAttributes.class), locale);

        assertEquals("admin/game_form", mav.getViewName());
    }

    @Test
    void editGame_ShouldReturnModelAndView() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        game.setId(id);
        game.setCategory(new Category());
        game.setCompany(new Company());
        when(gameService.findById(id)).thenReturn(game);
        when(categoryService.findAll()).thenReturn(List.of());
        when(companyService.findAll()).thenReturn(List.of());

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.editGame(id, locale);

        assertEquals("admin/game_form", mav.getViewName());
    }

    @Test
    void deleteGame_ShouldRedirect() {
        UUID id = UUID.randomUUID();

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.deleteGame(id, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/games", mav.getViewName());
        verify(gameService).deleteById(id);
    }

    @Test
    void addDiscountGet_ShouldReturnForm() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        when(gameService.findById(id)).thenReturn(game);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.addDiscount(id, locale);

        assertEquals("admin/discount_form", mav.getViewName());
    }

    @Test
    void addDiscountGet_WithDiscountAvailable() {
        UUID id = UUID.randomUUID();
        Discount discount = new Discount();
        discount.setAmount(15);
        discount.setType(DiscountType.FIXED);

        Game game = new Game();
        game.setDiscount(discount);
        when(gameService.findById(id)).thenReturn(game);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.addDiscount(id, locale);

        CreateDiscountRequest discountRequest = (CreateDiscountRequest) mav.getModel().get("discount");
        assertEquals("admin/discount_form", mav.getViewName());
        assertEquals(discount.getAmount(), discountRequest.getAmount());
        assertEquals(discount.getType(), discountRequest.getType());
    }

    @Test
    void addDiscount_Fails_WhenAmountIsZeroOrNegative() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        game.setTitle("Test Game");
        when(gameService.findById(id)).thenReturn(game);

        CreateDiscountRequest req = new CreateDiscountRequest();
        req.setAmount(0); // triggers first validation
        req.setType(DiscountType.PERCENT);

        BindingResult br = new BeanPropertyBindingResult(req, "discount");

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.addDiscount(id, req, br, mock(RedirectAttributes.class), locale);

        assertEquals("admin/discount_form", mv.getViewName());
        assertTrue(br.hasFieldErrors("amount"));
    }

    @Test
    void addDiscount_Fails_WhenPercentAbove100() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        game.setTitle("Test Game");
        when(gameService.findById(id)).thenReturn(game);

        CreateDiscountRequest req = new CreateDiscountRequest();
        req.setAmount(150);
        req.setType(DiscountType.PERCENT);

        BindingResult br = new BeanPropertyBindingResult(req, "discount");

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.addDiscount(id, req, br, mock(RedirectAttributes.class), locale);

        assertEquals("admin/discount_form", mv.getViewName());
        assertTrue(br.hasFieldErrors("amount"));
    }

    @Test
    void addDiscount_Fails_WhenStartDateAfterEndDate() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        game.setTitle("Test Game");
        when(gameService.findById(id)).thenReturn(game);

        CreateDiscountRequest req = new CreateDiscountRequest();
        req.setAmount(50);
        req.setType(DiscountType.PERCENT);
        req.setStartDate(LocalDateTime.now().plusDays(5));
        req.setEndDate(LocalDateTime.now());

        BindingResult br = new BeanPropertyBindingResult(req, "discount");

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.addDiscount(id, req, br, mock(RedirectAttributes.class), locale);

        assertEquals("admin/discount_form", mv.getViewName());
        assertTrue(br.hasFieldErrors("startDate"));
    }

    @Test
    void addDiscount_Success_NoValidationErrors() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        game.setTitle("Test Game");
        when(gameService.findById(id)).thenReturn(game);

        CreateDiscountRequest req = new CreateDiscountRequest();
        req.setAmount(50);
        req.setType(DiscountType.PERCENT);
        req.setStartDate(LocalDateTime.now().minusDays(1));
        req.setEndDate(LocalDateTime.now());

        BindingResult br = new BeanPropertyBindingResult(req, "discount");

        Discount discount = new Discount();
        when(discountService.persist(any(), eq(req))).thenReturn(discount);
        when(gameService.addDiscount(id, discount)).thenReturn(game);

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.addDiscount(id, req, br, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/games", mv.getViewName());
        assertFalse(br.hasErrors());
    }

    @Test
    void whenDiscountTypeIsFixedAndMoreThan100_thenDiscountIsAddedSuccessfully() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        game.setTitle("Test Game");
        when(gameService.findById(id)).thenReturn(game);

        CreateDiscountRequest req = new CreateDiscountRequest();
        req.setAmount(120);
        req.setType(DiscountType.FIXED);
        req.setStartDate(LocalDateTime.now().minusDays(1));
        req.setEndDate(LocalDateTime.now());

        BindingResult br = new BeanPropertyBindingResult(req, "discount");

        Discount discount = new Discount();
        when(discountService.persist(any(), eq(req))).thenReturn(discount);
        when(gameService.addDiscount(id, discount)).thenReturn(game);

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.addDiscount(id, req, br, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/games", mv.getViewName());
        assertFalse(br.hasErrors());
    }

    @Test
    void addDiscountPost_WithErrors_ShouldReturnForm() {
        UUID id = UUID.randomUUID();
        CreateDiscountRequest req = new CreateDiscountRequest();
        BindingResult result = mock(BindingResult.class);

        when(result.hasErrors()).thenReturn(true);
        when(gameService.findById(id)).thenReturn(new Game());

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.addDiscount(id, req, result, mock(RedirectAttributes.class), locale);

        assertEquals("admin/discount_form", mav.getViewName());
    }

    @Test
    void addDiscountPost_Success_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        when(gameService.findById(id)).thenReturn(game);

        CreateDiscountRequest req = new CreateDiscountRequest();
        BindingResult result = mock(BindingResult.class);

        when(result.hasErrors()).thenReturn(false);

        when(discountService.persist(any(), any())).thenReturn(new Discount());
        when(gameService.addDiscount(eq(id), any())).thenReturn(game);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.addDiscount(id, req, result, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/games", mav.getViewName());
    }

    @Test
    void removeDiscount_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        Game game = new Game();
        game.setDiscount(new Discount());

        when(gameService.findById(id)).thenReturn(game);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.removeDiscount(id, mock(RedirectAttributes.class), locale);

        assertEquals("redirect:/admin/games", mav.getViewName());
        verify(discountService).unsetDiscount(any());
    }

    @Test
    void editGame_Success_WithImageUpload() throws Exception {
        UUID id = UUID.randomUUID();

        CreateGameRequest req = new CreateGameRequest();
        req.setTitle("New Game");
        req.setCategoryId(UUID.randomUUID());
        req.setCompanyId(UUID.randomUUID());

        MultipartFile image = mock(MultipartFile.class);
        req.setImage(image);

        Game game = new Game();
        game.setId(id);
        game.setImage("/old/path.png");

        Category category = new Category();
        Company company = new Company();

        when(categoryService.findById(req.getCategoryId())).thenReturn(category);
        when(companyService.findById(req.getCompanyId())).thenReturn(company);
        when(image.isEmpty()).thenReturn(false);
        when(image.getOriginalFilename()).thenReturn("имя.png");
        when(image.getBytes()).thenReturn("content".getBytes());

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.editGame(id, req, br, redirectAttributes, locale);

        assertEquals("redirect:/admin/games", mv.getViewName());
        verify(gameService).edit(eq(id), eq(req), eq(category), eq(company), anyString());
    }

    @Test
    void editGame_Success_NoImage() throws Exception {
        UUID id = UUID.randomUUID();

        CreateGameRequest req = new CreateGameRequest();
        req.setTitle("New Game");
        req.setCategoryId(UUID.randomUUID());
        req.setCompanyId(UUID.randomUUID());
        req.setImage(null);

        Game game = new Game();
        game.setId(id);

        Category category = new Category();
        Company company = new Company();

        when(categoryService.findById(req.getCategoryId())).thenReturn(category);
        when(companyService.findById(req.getCompanyId())).thenReturn(company);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.editGame(id, req, br, redirectAttributes, locale);

        assertEquals("redirect:/admin/games", mv.getViewName());
        verify(gameService).edit(id, req, category, company, null);
    }

    @Test
    void editGame_Success_EmptyImage() throws Exception {
        UUID id = UUID.randomUUID();

        CreateGameRequest req = new CreateGameRequest();
        req.setTitle("New Game");
        req.setCategoryId(UUID.randomUUID());
        req.setCompanyId(UUID.randomUUID());

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        req.setImage(file);

        Game game = new Game();
        game.setId(id);

        Category category = new Category();
        Company company = new Company();

        when(categoryService.findById(req.getCategoryId())).thenReturn(category);
        when(companyService.findById(req.getCompanyId())).thenReturn(company);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.editGame(id, req, br, redirectAttributes, locale);

        assertEquals("redirect:/admin/games", mv.getViewName());
        verify(gameService).edit(id, req, category, company, null);
    }

    @Test
    void editGame_Fails_WithBindingErrors() throws Exception {
        UUID id = UUID.randomUUID();

        CreateGameRequest req = new CreateGameRequest();
        req.setTitle("");

        Game game = new Game();
        game.setId(id);
        game.setImage("/old/path.png");

        when(gameService.findById(id)).thenReturn(game);
        when(categoryService.findAll()).thenReturn(List.of());
        when(companyService.findAll()).thenReturn(List.of());

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        Locale locale = Locale.ENGLISH;
        ModelAndView mv = controller.editGame(id, req, br, redirectAttributes, locale);

        assertEquals("admin/game_form", mv.getViewName());
        assertEquals(req, mv.getModel().get("game"));
        verify(gameService, never()).edit(any(), any(), any(), any(), any());
    }
}

