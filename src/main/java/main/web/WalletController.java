package main.web;

import main.model.User;
import main.security.AuthenticationDetails;
import main.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
}
