package main.utils;

import main.model.UserRole;
import main.security.AuthenticationDetails;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private UserService userService;

    @ModelAttribute("balance")
    public BigDecimal addUserBalance(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        if (userDetails == null || userDetails.getRole().equals(UserRole.ADMIN)) {
            return null;
        }

        return userService.getById(userDetails.getId()).getBalance();
    }
}
