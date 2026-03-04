package project.web.admin;

import org.springframework.web.bind.annotation.*;
import project.model.User;
import project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import project.utils.ImagesCleanupService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class UsersAdminController {

    private final UserService userService;
    private final ImagesCleanupService imagesCleanupService;
    private final Logger logger;

    public UsersAdminController(UserService userService, ImagesCleanupService imagesCleanupService) {
        this.userService = userService;
        this.imagesCleanupService = imagesCleanupService;
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

    @PostMapping("/delete-avatars")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseBody
    public Map<String, String> removeAvatars() {
        imagesCleanupService.deleteUnusedUserAvatars(logger);

        return Map.of("message", "Unused user avatars deleted successfully!");
    }
}
