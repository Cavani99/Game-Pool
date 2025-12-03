package project.web.admin;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import project.web.dto.EditProfileRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final Logger logger;

    public AdminController(UserService userService) {
        this.userService = userService;
        this.logger = LoggerFactory.getLogger(AdminController.class);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView getHomepage(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/admin");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("page", "profile");
        modelAndView.addObject("title", "Profile");

        return modelAndView;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editAdmin(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/edit-profile");

        User user = userService.getById(userDetails.getId());

        EditProfileRequest editProfileRequest = new EditProfileRequest(user.getUsername(), null, user.getAvatar());

        modelAndView.addObject("user", editProfileRequest);
        modelAndView.addObject("page", "profile");
        modelAndView.addObject("title", "Profile");

        logger.info("User " + user.getUsername() + " editing profile!");

        return modelAndView;
    }

    @PostMapping("/profile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView editProfile(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("user") EditProfileRequest editProfileRequest,
                                    BindingResult bindingResult) throws IOException {

        User user = userService.getById(userDetails.getId());

        if (userService.findByUsername(user.getId(), editProfileRequest.getUsername())) {
            bindingResult.rejectValue("username", "username.empty", "Please, write unique username!");
        }

        if (bindingResult.hasErrors()) {
            logger.error("Admin {} had errors editing profile: {}", user.getUsername(), bindingResult.getAllErrors());
            editProfileRequest.setAvatarPath(user.getAvatar());

            ModelAndView mav = new ModelAndView("admin/edit-profile");
            mav.addObject("game", editProfileRequest);
            mav.addObject("page", "profile");
            mav.addObject("title", "Profile");
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
        logger.info("Admin {} edited his profile", user.getUsername());

        return new ModelAndView("redirect:/admin");
    }
}
