package project.web;

import jakarta.validation.Valid;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.NotificationService;
import project.service.UserService;
import project.web.dto.ChangePasswordRequest;
import project.web.dto.EditProfileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final Logger logger;
    private final MessageService messageService;

    public DashboardController(UserService userService, NotificationService notificationService, MessageService messageService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.logger = LoggerFactory.getLogger(DashboardController.class);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getHomepage(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("logged", true);
        modelAndView.addObject("notifications_count", notificationService.getNotificationsByUser(user.getId()).size());
        modelAndView.addObject("page", "home");
        modelAndView.addObject("title", "Home");

        return modelAndView;
    }

    @GetMapping("/edit_profile")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView editProfile(@AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile_edit");

        User user = userService.getById(userDetails.getId());

        EditProfileRequest editProfileRequest = new EditProfileRequest(user.getUsername(), null, user.getAvatar());

        modelAndView.addObject("user", editProfileRequest);
        modelAndView.addObject("page", "home");
        modelAndView.addObject("title", "Home");
        String message = messageService.getLocalizedMessage("user.edit", locale);
        logger.info(message, userDetails.getUsername());

        return modelAndView;
    }

    @PostMapping("/edit_profile")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView editProfile(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("user") EditProfileRequest editProfileRequest,
                                    BindingResult bindingResult, Locale locale) throws IOException {

        User user = userService.getById(userDetails.getId());

        if (userService.findByUsername(user.getId(), editProfileRequest.getUsername())) {
            String message = messageService.getLocalizedMessage("unique_username", locale);
            bindingResult.rejectValue("username", "username.empty", message);
        }

        if (bindingResult.hasErrors()) {
            String message = messageService.getLocalizedMessage("user.edit_errors", locale);
            logger.error(message, user.getUsername(), bindingResult.getAllErrors());
            editProfileRequest.setAvatarPath(user.getAvatar());

            ModelAndView mav = new ModelAndView("profile_edit");
            mav.addObject("game", editProfileRequest);
            mav.addObject("page", "home");
            mav.addObject("title", "Home");
            return mav;
        }

        MultipartFile avatarFile = editProfileRequest.getAvatar();
        String avatarPath = null;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String uploadDir = "uploads/avatars/";
            Files.createDirectories(Paths.get(uploadDir));

            String originalName = avatarFile.getOriginalFilename();
            String latinName = Normalizer.normalize(originalName, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("[^a-zA-Z0-9._-]", "_");

            String filename = UUID.randomUUID() + "_" + latinName;
            Path filePath = Paths.get(uploadDir + filename);
            Files.write(filePath, avatarFile.getBytes());

            avatarPath = "/uploads/avatars/" + filename;
        }

        userService.edit(user.getId(), editProfileRequest, avatarPath);
        String message = messageService.getLocalizedMessage("user.edited", locale);
        logger.info(message, user.getUsername());

        return new ModelAndView("redirect:/dashboard");
    }

    @GetMapping("/change_password")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView changePassword(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile_change_password");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", new ChangePasswordRequest());
        modelAndView.addObject("avatar", user.getAvatar());
        modelAndView.addObject("page", "home");
        modelAndView.addObject("title", "Home");

        return modelAndView;
    }

    @PostMapping("/change_password")
    public ModelAndView changePassword(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("user") ChangePasswordRequest changePasswordRequest,
                                       BindingResult bindingResult, Locale locale) {
        if (!changePasswordRequest.getPassword().equals(changePasswordRequest.getRepeat_password())) {
            String message = messageService.getLocalizedMessage("password_match", locale);
            bindingResult.rejectValue("password", "password.empty", message);
        }

        User user = userService.getById(userDetails.getId());
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("profile_change_password");
            mav.addObject("user", changePasswordRequest);
            mav.addObject("avatar", user.getAvatar());
            mav.addObject("page", "home");
            mav.addObject("title", "Home");

            String message = messageService.getLocalizedMessage("user.password_change_error", locale);
            logger.error(message, user.getUsername(), bindingResult.getAllErrors());

            return mav;
        }

        userService.changePassword(user.getId(), changePasswordRequest);
        String message = messageService.getLocalizedMessage("user.password_changed", locale);
        logger.info(message, user.getUsername());

        return new ModelAndView("redirect:/dashboard");
    }
}
