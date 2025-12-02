package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import project.model.User;
import project.model.UserRole;
import project.security.AuthenticationDetails;
import project.service.UserService;
import project.web.HomeController;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeControllerUnitTests {

    @Mock
    UserService userService;

    @InjectMocks
    HomeController controller;

    @Test
    void testAdminRedirect() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setRole(UserRole.ADMIN);

        when(userService.getById(details.getId())).thenReturn(user);

        ModelAndView mav = controller.getHome(details);

        assertEquals("redirect:/admin", mav.getViewName());
    }

    @Test
    void testUserRedirect() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setRole(UserRole.USER);

        when(userService.getById(details.getId())).thenReturn(user);

        ModelAndView mav = controller.getHome(details);

        assertEquals("redirect:/dashboard", mav.getViewName());
    }

}

