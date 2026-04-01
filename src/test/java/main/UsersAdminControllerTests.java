package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import project.model.User;
import project.service.MessageService;
import project.service.UserService;
import project.web.admin.UsersAdminController;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersAdminControllerTests {

    @Mock
    private UserService userService;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private UsersAdminController controller;

    @Test
    void getUsers_ShouldReturnCorrectModelAndView() {
        List<User> mockUsers = List.of(new User(), new User());
        when(userService.findAllUsers()).thenReturn(mockUsers);

        ModelAndView mav = controller.getUsers();

        assertEquals("admin/users", mav.getViewName());
        assertEquals(mockUsers, mav.getModel().get("users"));
        assertEquals("users", mav.getModel().get("page"));
        assertEquals("Users", mav.getModel().get("title"));

        verify(userService).findAllUsers();
    }

    @Test
    void showUser_ShouldReturnCorrectUserInModel() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userService.getById(id)).thenReturn(user);

        ModelAndView mav = controller.showUser(id);

        assertEquals("admin/user_form", mav.getViewName());
        assertEquals(user, mav.getModel().get("user"));
        assertEquals("users", mav.getModel().get("page"));
        assertEquals("Users", mav.getModel().get("title"));

        verify(userService).getById(id);
    }

    @Test
    void changeBanValue_ShouldCallServiceAndRedirect() {
        UUID id = UUID.randomUUID();

        ModelAndView mav = controller.changeBanValue(id);

        assertEquals("redirect:/admin/users", mav.getViewName());

        verify(userService).changeBanStatus(eq(id), any());
    }

}

