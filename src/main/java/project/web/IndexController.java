package project.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.NotificationService;
import project.service.UserService;
import project.web.dto.LoginRequest;
import project.web.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
public class IndexController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final Logger logger;

    private final MessageService messageService;

    public IndexController(UserService userService, NotificationService notificationService, MessageService messageService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.logger = LoggerFactory.getLogger(IndexController.class);
    }

    @GetMapping
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegister(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        if (userDetails != null) {
            return new ModelAndView("redirect:/dashboard");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("user", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid @ModelAttribute("user") RegisterRequest registerRequest, BindingResult bindingResult, Locale locale) throws IOException {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("register");
            mav.addObject("user", registerRequest);

            String message = messageService.getLocalizedMessage("register.errors", locale);
            logger.error(message, bindingResult.getAllErrors());

            return mav;
        }

        MultipartFile avatarFile = registerRequest.getAvatar();
        String avatarPath;
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
        } else {
            String message = messageService.getLocalizedMessage("register.no_image", locale);
            ModelAndView mav = new ModelAndView("register");
            mav.addObject("user", registerRequest);
            mav.addObject("errorMessage", message);

            return mav;
        }

        User createdUser = userService.create(registerRequest, avatarPath);
        notificationService.saveUser(createdUser.getId(), createdUser.getUsername());
        String message = messageService.getLocalizedMessage("register.user_created", locale);
        logger.info(message, createdUser.getUsername(), createdUser.getRole());

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView getLogin(@RequestParam(name = "error", required = false) String errorMessage, @AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        if (userDetails != null) {
            return new ModelAndView("redirect:/dashboard");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("user", new LoginRequest());

        if (errorMessage != null) {
            String message = messageService.getLocalizedMessage("login.error", locale);
            modelAndView.addObject("error", message);
        }

        return modelAndView;
    }
}
