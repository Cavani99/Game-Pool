package project.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.model.Transaction;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.TransactionService;
import project.service.UserService;
import project.web.dto.AddFundsRequest;
import project.web.dto.SendFundsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("dashboard/wallet")
public class WalletController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final Logger logger;
    private final MessageService messageService;

    public WalletController(UserService userService, TransactionService transactionService, MessageService messageService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.messageService = messageService;
        this.logger = LoggerFactory.getLogger(WalletController.class);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getWalletView(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet");

        User user = userService.getById(userDetails.getId());
        List<Transaction> transactions = transactionService.getByUser(user);

        modelAndView.addObject("user", user);
        modelAndView.addObject("transactions", transactions);
        modelAndView.addObject("page", "wallet");
        modelAndView.addObject("title", "Wallet");

        return modelAndView;
    }

    @GetMapping("add")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView addWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet_add");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("add_funds", new AddFundsRequest());
        modelAndView.addObject("page", "wallet");
        modelAndView.addObject("title", "Wallet");

        String message = messageService.getLocalizedMessage("wallet_form_add_funds", locale);
        logger.info(message, user.getUsername());

        return modelAndView;
    }

    @PostMapping("add")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView addWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("add_funds") AddFundsRequest addFundsRequest,
                                       BindingResult bindingResult, Locale locale) {
        User user = userService.getById(userDetails.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("wallet_add");

            mav.addObject("user", user);
            mav.addObject("add_funds", addFundsRequest);
            mav.addObject("page", "wallet");
            mav.addObject("title", "Wallet");

            String message = messageService.getLocalizedMessage("wallet_add_funds_error", locale);
            logger.error(message, user.getUsername(), bindingResult.getAllErrors());

            return mav;
        }

        userService.addFunds(userDetails.getId(), addFundsRequest);
        transactionService.createSelfTransaction(userDetails.getId(), addFundsRequest);
        String message = messageService.getLocalizedMessage("wallet_funds_added", locale);
        logger.info(message, addFundsRequest.getAmount(), user.getUsername());

        return new ModelAndView("redirect:/dashboard/wallet");
    }

    @PostMapping("add_ajax")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseBody
    public ResponseEntity<?> addWalletFundsAjax(
            @AuthenticationPrincipal AuthenticationDetails userDetails,
            @RequestBody AddFundsRequest addFundsRequest) {
        userService.addFunds(userDetails.getId(), addFundsRequest);
        transactionService.createSelfTransaction(userDetails.getId(), addFundsRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("send")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView sendWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet_send");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("friends", user.getFriends());
        modelAndView.addObject("send_request", new SendFundsRequest());
        modelAndView.addObject("page", "wallet");
        modelAndView.addObject("title", "Wallet");

        String message = messageService.getLocalizedMessage("wallet_form_send_funds", locale);
        logger.info(message, user.getUsername());

        return modelAndView;
    }

    @PostMapping("send")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView sendWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("send_request") SendFundsRequest sendFundsRequest,
                                        BindingResult bindingResult, Locale locale) {
        User user = userService.getById(userDetails.getId());

        if (sendFundsRequest.getAmount() != null && sendFundsRequest.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
                !userService.hasFunds(userDetails.getId(), sendFundsRequest.getAmount())) {

            String message = messageService.getLocalizedMessage("wallet_not_enough_funds", locale);
            bindingResult.rejectValue("amount", "amount.empty", message);
        }

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("wallet_send");

            mav.addObject("user", user);
            mav.addObject("friends", user.getFriends());
            mav.addObject("send_request", sendFundsRequest);
            mav.addObject("page", "wallet");
            mav.addObject("title", "Wallet");

            String message = messageService.getLocalizedMessage("wallet_send_funds_error", locale);
            logger.error(message, user.getUsername(), bindingResult.getAllErrors());

            return mav;
        }

        userService.sendFunds(userDetails.getId(), sendFundsRequest);
        transactionService.createSendFundsTransaction(userDetails.getId(), sendFundsRequest);

        User friend = userService.getById(sendFundsRequest.getFriend());

        String message = messageService.getLocalizedMessage("wallet_funds_send", locale);
        logger.info(message, sendFundsRequest.getAmount(), friend.getUsername(), user.getUsername());

        return new ModelAndView("redirect:/dashboard/wallet");
    }
}
