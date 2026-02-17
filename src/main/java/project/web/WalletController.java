package project.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.model.Transaction;
import project.model.User;
import project.security.AuthenticationDetails;
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

@Controller
@RequestMapping("dashboard/wallet")
public class WalletController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final Logger logger;

    public WalletController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
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
    public ModelAndView addWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet_add");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("add_funds", new AddFundsRequest());
        modelAndView.addObject("page", "wallet");
        modelAndView.addObject("title", "Wallet");
        logger.info("Form for adding funds to user {} opened", user.getUsername());

        return modelAndView;
    }

    @PostMapping("add")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView addWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("add_funds") AddFundsRequest addFundsRequest,
                                       BindingResult bindingResult) {
        User user = userService.getById(userDetails.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("wallet_add");

            mav.addObject("user", user);
            mav.addObject("add_funds", addFundsRequest);
            mav.addObject("page", "wallet");
            mav.addObject("title", "Wallet");

            logger.error("Errors in adding funds to {}: {}", user.getUsername(), bindingResult.getAllErrors());

            return mav;
        }

        userService.addFunds(userDetails.getId(), addFundsRequest);
        transactionService.createSelfTransaction(userDetails.getId(), addFundsRequest);
        logger.info("{} € of funds added to user {}", addFundsRequest.getAmount(), user.getUsername());

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
    public ModelAndView sendWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet_send");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("friends", user.getFriends());
        modelAndView.addObject("send_request", new SendFundsRequest());
        modelAndView.addObject("page", "wallet");
        modelAndView.addObject("title", "Wallet");

        logger.info("Form for sending funds to a friend opened from user {}", user.getUsername());

        return modelAndView;
    }

    @PostMapping("send")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView sendWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("send_request") SendFundsRequest sendFundsRequest,
                                        BindingResult bindingResult) {
        User user = userService.getById(userDetails.getId());

        if (sendFundsRequest.getAmount() != null && sendFundsRequest.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
                !userService.hasFunds(userDetails.getId(), sendFundsRequest.getAmount())) {
            bindingResult.rejectValue("amount", "amount.empty", "The user does not have enough funds to send!");
        }

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("wallet_send");

            mav.addObject("user", user);
            mav.addObject("friends", user.getFriends());
            mav.addObject("send_request", sendFundsRequest);
            mav.addObject("page", "wallet");
            mav.addObject("title", "Wallet");

            logger.error("Errors in sending funds from user {}: {}", user.getUsername(), bindingResult.getAllErrors());

            return mav;
        }

        userService.sendFunds(userDetails.getId(), sendFundsRequest);
        transactionService.createSendFundsTransaction(userDetails.getId(), sendFundsRequest);

        User friend = userService.getById(sendFundsRequest.getFriend());
        logger.info("{} € of funds sent to friend {} from user {}", sendFundsRequest.getAmount(), friend.getUsername(), user.getUsername());

        return new ModelAndView("redirect:/dashboard/wallet");
    }
}
