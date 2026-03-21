package project.utils;

import project.model.UserRole;
import project.security.AuthenticationDetails;
import project.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@ControllerAdvice
public class GlobalModelAttributes {

    private final List<Locale> supportedLocales;
    private final UserService userService;

    public GlobalModelAttributes(List<Locale> supportedLocales, UserService userService) {
        this.supportedLocales = supportedLocales;
        this.userService = userService;
    }

    @ModelAttribute("balance")
    public BigDecimal addUserBalance(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        if (userDetails == null || userDetails.getRole() == UserRole.ADMIN) {
            return null;
        }

        return userService.getById(userDetails.getId()).getBalance();
    }

    @ModelAttribute("currentLocale")
    public Locale currentLocale(Locale locale) {
        return locale;
    }

    @ModelAttribute("locales")
    public List<Locale> locales() {
        return supportedLocales;
    }
}
