package main.web;

import jakarta.validation.Valid;
import main.model.User;
import main.security.AuthenticationDetails;
import main.service.UserService;
import main.web.dto.AddFundsRequest;
import main.web.dto.SendFundsRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;

@Controller
@RequestMapping("dashboard/wallet")
public class WalletController {

    private final UserService userService;

    public WalletController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getWalletView(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
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
            return mav;
        }

        userService.addFunds(userDetails.getId(), addFundsRequest);

        return new ModelAndView("redirect:/dashboard/wallet");
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

        return modelAndView;
    }

    @PostMapping("send")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView sendWalletFunds(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("send_request") SendFundsRequest sendFundsRequest,
                                        BindingResult bindingResult) {
        User user = userService.getById(userDetails.getId());

        if (!userService.hasFunds(userDetails.getId(), sendFundsRequest.getAmount())
                && sendFundsRequest.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            bindingResult.rejectValue("amount", "amount.empty", "The user does not have enough funds to send!");
        }

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("wallet_send");

            mav.addObject("user", user);
            mav.addObject("friends", user.getFriends());
            mav.addObject("send_request", sendFundsRequest);
            mav.addObject("page", "wallet");
            mav.addObject("title", "Wallet");
            return mav;
        }

        userService.sendFunds(userDetails.getId(), sendFundsRequest);

        return new ModelAndView("redirect:/dashboard/wallet");
    }
}
