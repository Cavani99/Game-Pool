package main;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import project.model.Transaction;
import project.model.User;
import project.model.UserRole;
import project.security.AuthenticationDetails;
import project.service.TransactionService;
import project.service.UserService;
import project.web.WalletController;
import project.web.dto.AddFundsRequest;
import project.web.dto.SendFundsRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerUnitTests {

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private WalletController walletController;


    @Test
    void testGetWalletView() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());

        when(userService.getById(details.getId()))
                .thenReturn(user);
        when(transactionService.getByUser(user))
                .thenReturn(List.of(new Transaction()));

        ModelAndView mv = walletController.getWalletView(details);

        assertEquals("wallet", mv.getViewName());
        assertEquals(user, mv.getModel().get("user"));
        assertEquals("wallet", mv.getModel().get("page"));
        assertEquals("Wallet", mv.getModel().get("title"));

        verify(transactionService).getByUser(user);
    }

    @Test
    void testGetAddWalletFunds() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        when(userService.getById(details.getId()))
                .thenReturn(user);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.addWalletFunds(details, locale);

        assertEquals("wallet_add", mv.getViewName());
        assertEquals(user, mv.getModel().get("user"));
        assertEquals(AddFundsRequest.class, mv.getModel().get("add_funds").getClass());
    }

    @Test
    void testPostAddWalletFundsErrors() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        AddFundsRequest req = new AddFundsRequest();
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getById(details.getId()))
                .thenReturn(user);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.addWalletFunds(details, req, bindingResult, locale);

        assertEquals("wallet_add", mv.getViewName());
        assertEquals(req, mv.getModel().get("add_funds"));

        verify(userService, never()).addFunds(eq(UUID.randomUUID()), any());
        verify(transactionService, never()).createSelfTransaction(eq(UUID.randomUUID()), any());
    }

    @Test
    void testPostAddWalletFundsSuccess() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        AddFundsRequest req = new AddFundsRequest();
        req.setAmount(BigDecimal.TEN);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getById(details.getId()))
                .thenReturn(user);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.addWalletFunds(details, req, bindingResult, locale);

        assertEquals("redirect:/dashboard/wallet", mv.getViewName());

        verify(userService).addFunds(details.getId(), req);
        verify(transactionService).createSelfTransaction(details.getId(), req);
    }

    @Test
    void testGetSendFunds() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        when(userService.getById(details.getId()))
                .thenReturn(user);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.sendWalletFunds(details, locale);

        assertEquals("wallet_send", mv.getViewName());
        assertEquals(user, mv.getModel().get("user"));
        assertEquals(user.getFriends(), mv.getModel().get("friends"));
        assertEquals(SendFundsRequest.class, mv.getModel().get("send_request").getClass());
    }

    @Test
    void testPostSendFundsInsufficientFunds() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        UUID friendId = UUID.randomUUID();

        SendFundsRequest req = new SendFundsRequest();
        req.setAmount(BigDecimal.valueOf(100));
        req.setFriend(friendId);

        BindingResult bindingResult = new BeanPropertyBindingResult(req, "send_request");

        when(userService.getById(details.getId()))
                .thenReturn(user);
        when(userService.hasFunds(details.getId(), BigDecimal.valueOf(100)))
                .thenReturn(false);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.sendWalletFunds(details, req, bindingResult, locale);

        assertEquals("wallet_send", mv.getViewName());

        verify(transactionService, never()).createSendFundsTransaction(eq(details.getId()), any());
    }

    @Test
    void testPostSendFundsWhenSendFundsAmountIsNull() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        UUID friendId = UUID.randomUUID();

        SendFundsRequest req = new SendFundsRequest();
        req.setAmount(null);
        req.setFriend(friendId);

        // VALIDATE manually (this is what Spring normally does automatically)
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        BindingResult bindingResult =
                new BeanPropertyBindingResult(req, "send_request");

        validator.validate(req).forEach(violation ->
                bindingResult.rejectValue(violation.getPropertyPath().toString(),
                        violation.getMessage(), violation.getMessage())
        );

        when(userService.getById(details.getId())).thenReturn(user);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.sendWalletFunds(details, req, bindingResult, locale);

        assertEquals("wallet_send", mv.getViewName());
        assertTrue(bindingResult.hasErrors());

        verify(transactionService, never())
                .createSendFundsTransaction(eq(details.getId()), any());
    }

    @Test
    void whenPostSendFundsIsZeroOrLess_thenGetErrors() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        UUID friendId = UUID.randomUUID();

        SendFundsRequest req = new SendFundsRequest();
        req.setAmount(BigDecimal.valueOf(-2));
        req.setFriend(friendId);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        BindingResult bindingResult =
                new BeanPropertyBindingResult(req, "send_request");

        validator.validate(req).forEach(violation ->
                bindingResult.rejectValue(violation.getPropertyPath().toString(),
                        violation.getMessage(), violation.getMessage())
        );

        when(userService.getById(details.getId())).thenReturn(user);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.sendWalletFunds(details, req, bindingResult, locale);

        assertEquals("wallet_send", mv.getViewName());
        assertTrue(bindingResult.hasErrors());

        verify(transactionService, never())
                .createSendFundsTransaction(eq(details.getId()), any());
    }

    @Test
    void testPostSendFundsSuccess() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User user = new User();
        user.setId(details.getId());
        user.setUsername(details.getUsername());

        UUID friendId = UUID.randomUUID();
        SendFundsRequest req = new SendFundsRequest();
        req.setAmount(BigDecimal.valueOf(50));
        req.setFriend(friendId);

        User friend = new User();
        friend.setId(friendId);
        friend.setUsername("mike");

        when(userService.getById(details.getId()))
                .thenReturn(user);
        when(userService.hasFunds(details.getId(), req.getAmount())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getById(friendId)).thenReturn(friend);

        Locale locale = Locale.getDefault();
        ModelAndView mv = walletController.sendWalletFunds(details, req, bindingResult, locale);

        assertEquals("redirect:/dashboard/wallet", mv.getViewName());

        verify(userService).sendFunds(details.getId(), req);
        verify(transactionService).createSendFundsTransaction(details.getId(), req);
    }
}
