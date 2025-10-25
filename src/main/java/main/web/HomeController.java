package main.web;

import main.model.User;
import main.model.UserRole;
import main.security.AuthenticationDetails;
import main.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequestMapping("/home")
    public ModelAndView getHome(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        User user = userService.getById(userDetails.getId());

        if (user.getRole().equals(UserRole.ADMIN)) {
            return new ModelAndView("redirect:/admin");
        }

        if (user.getRole().equals(UserRole.USER)) {
            return new ModelAndView("redirect:/dashboard");
        }

        return new ModelAndView("redirect:/login?error");
    }
}
