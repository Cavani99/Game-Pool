package main.web;

import jakarta.validation.Valid;
import main.model.User;
import main.security.AuthenticationDetails;
import main.service.UserService;
import main.web.dto.ChangePasswordRequest;
import main.web.dto.EditProfileRequest;
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
import java.util.UUID;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getHomepage(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("logged", true);
        modelAndView.addObject("page", "home");
        modelAndView.addObject("title", "Home");

        return modelAndView;
    }

    @GetMapping("/edit_profile")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView editProfile(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile_edit");

        User user = userService.getById(userDetails.getId());

        EditProfileRequest editProfileRequest = new EditProfileRequest(user.getUsername(), null, user.getAvatar());

        modelAndView.addObject("user", editProfileRequest);
        modelAndView.addObject("page", "home");
        modelAndView.addObject("title", "Home");

        return modelAndView;
    }

    @PostMapping("/edit_profile")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView editProfile(@AuthenticationPrincipal AuthenticationDetails userDetails, @Valid @ModelAttribute("user") EditProfileRequest editProfileRequest,
                                    BindingResult bindingResult) throws IOException {

        User user = userService.getById(userDetails.getId());

        if (userService.findByUsername(user.getId(), editProfileRequest.getUsername())) {
            bindingResult.rejectValue("username", "username.empty", "Please, write unique username!");
        }

        if (bindingResult.hasErrors()) {
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
                                       BindingResult bindingResult) {
        if (!changePasswordRequest.getPassword().equals(changePasswordRequest.getRepeat_password())) {
            bindingResult.rejectValue("password", "password.empty", "Both password must match!");
        }

        User user = userService.getById(userDetails.getId());
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("profile_change_password");
            mav.addObject("user", changePasswordRequest);
            mav.addObject("avatar", user.getAvatar());
            mav.addObject("page", "home");
            mav.addObject("title", "Home");
            return mav;
        }

        userService.changePassword(user.getId(), changePasswordRequest);

        return new ModelAndView("redirect:/dashboard");
    }
}
