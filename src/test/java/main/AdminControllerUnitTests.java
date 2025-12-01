package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.UserService;
import project.web.admin.AdminController;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

import org.springframework.web.servlet.ModelAndView;


@ExtendWith(MockitoExtension.class)
public class AdminControllerUnitTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getHomepage_ShouldReturnCorrectModelAndView() {
        UUID id = UUID.randomUUID();

        AuthenticationDetails mockAuth = mock(AuthenticationDetails.class);
        when(mockAuth.getId()).thenReturn(id);

        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("AdminUser");

        when(userService.getById(id)).thenReturn(mockUser);

        ModelAndView mav = adminController.getHomepage(mockAuth);

        assertNotNull(mav);
        assertEquals("admin/admin", mav.getViewName());

        assertEquals(mockUser, mav.getModel().get("user"));
        assertEquals("profile", mav.getModel().get("page"));
        assertEquals("Profile", mav.getModel().get("title"));


        verify(userService).getById(id);
    }
}
