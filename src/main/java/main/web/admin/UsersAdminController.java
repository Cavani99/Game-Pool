package main.web.admin;

import main.model.User;
import main.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class UsersAdminController {

    private final UserService userService;
    private final Logger logger;

    public UsersAdminController(UserService userService) {
        this.userService = userService;
        this.logger = LoggerFactory.getLogger(UsersAdminController.class);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView getUsers() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/users");

        List<User> users = userService.findAllUsers();

        modelAndView.addObject("users", users);
        modelAndView.addObject("page", "users");
        modelAndView.addObject("title", "Users");

        return modelAndView;
    }

    @GetMapping("/show/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView showUser(@PathVariable("id") UUID id) {
        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/user_form");
        modelAndView.addObject("user", user);
        modelAndView.addObject("page", "users");
        modelAndView.addObject("title", "Users");

        return modelAndView;
    }

    @GetMapping("/ban/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView changeBanValue(@PathVariable("id") UUID id) {
        userService.changeBanStatus(id, logger);

        return new ModelAndView("redirect:/admin/users");
    }
}
