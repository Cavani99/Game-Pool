package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import project.model.User;
import project.model.UserRole;
import project.service.NotificationService;
import project.service.UserService;
import project.web.IndexController;
import project.web.dto.RegisterRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexControllerUnitTests {

    @Mock
    UserService userService;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    IndexController controller;

    @Test
    void testGetRegister() {
        ModelAndView mav = controller.getRegister(null);
        assertEquals("register", mav.getViewName());
        assertInstanceOf(RegisterRequest.class, mav.getModel().get("user"));
    }

    @Test
    void register_Success_ShouldRedirect() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setPassword("test123");

        MultipartFile mockAvatar = mock(MultipartFile.class);
        req.setAvatar(mockAvatar);

        when(mockAvatar.isEmpty()).thenReturn(false);
        when(mockAvatar.getOriginalFilename()).thenReturn("имя.png");
        when(mockAvatar.getBytes()).thenReturn("dummy".getBytes());

        User created = new User();
        created.setId(UUID.randomUUID());
        created.setUsername("john");
        created.setRole(UserRole.USER);

        when(userService.create(eq(req), anyString()))
                .thenReturn(created);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mv = controller.register(req, bindingResult);

        assertEquals("redirect:/login", mv.getViewName());
        verify(userService).create(eq(req), anyString());
        verify(notificationService).saveUser(created.getId(), "john");
    }

    @Test
    void testRegister_SuccessWithoutAvatar() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setPassword("test123");
        req.setAvatar(null);

        BindingResult bindingResult = new BeanPropertyBindingResult(req, "user");

        ModelAndView mv = controller.register(req, bindingResult);

        assertEquals("register", mv.getViewName());
        assertEquals("You need to pick an image!", mv.getModel().get("errorMessage"));

        verify(userService, never()).create(any(), any());
    }

    @Test
    void register_Success_WhenAvatarIsEmpty() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setPassword("pass");

        MultipartFile avatar = mock(MultipartFile.class);
        when(avatar.isEmpty()).thenReturn(true);
        req.setAvatar(avatar);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mv = controller.register(req, bindingResult);

        assertEquals("register", mv.getViewName());
        assertEquals("You need to pick an image!", mv.getModel().get("errorMessage"));

        verify(userService, never()).create(any(), any());
    }


    @Test
    void testRegister_WithValidationErrors() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("");
        req.setEmail("");

        BindingResult bindingResult = new BeanPropertyBindingResult(req, "user");
        bindingResult.rejectValue("username", "username.empty", "Username required");

        ModelAndView mv = controller.register(req, bindingResult);

        assertEquals("register", mv.getViewName());
        assertEquals(req, mv.getModel().get("user"));

        verify(userService, never()).create(any(), any());
        verify(notificationService, never()).saveUser(any(), any());
    }

    @Test
    void testLogin() {
        ModelAndView mav = controller.getLogin(null, null);

        assertEquals("login", mav.getViewName());
        assertNull(mav.getModel().get("error"));
    }

    @Test
    void testLoginErrorMessage() {
        ModelAndView mav = controller.getLogin("error", null);

        assertEquals("login", mav.getViewName());
        assertEquals("Invalid email or password!", mav.getModel().get("error"));

    }

    @Test
    void testIndex() {
        assertEquals("index", controller.index());
    }
}

