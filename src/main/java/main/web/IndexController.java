package main.web;

import jakarta.validation.Valid;
import main.model.User;
import main.service.NotificationService;
import main.service.UserService;
import main.web.dto.LoginRequest;
import main.web.dto.RegisterRequest;
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
import java.util.UUID;

@Controller
public class IndexController {

    private final UserService userService;
    private final NotificationService notificationService;

    public IndexController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegister() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("user", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid @ModelAttribute("user") RegisterRequest registerRequest, BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("register");
            mav.addObject("user", registerRequest);
            return mav;
        }

        MultipartFile avatarFile = registerRequest.getAvatar();
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

        User createdUser = userService.create(registerRequest, avatarPath);
        notificationService.saveUser(createdUser.getId());

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView getLogin(@RequestParam(name = "error", required = false) String errorMessage) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("user", new LoginRequest());

        if (errorMessage != null) {
            modelAndView.addObject("error", "Invalid username or password!");
        }

        return modelAndView;
    }
}
