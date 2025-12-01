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
import project.security.AuthenticationDetails;
import project.service.NotificationService;
import project.service.UserService;
import project.web.DashboardController;
import project.web.dto.ChangePasswordRequest;
import project.web.dto.EditProfileRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerUnitTests {

    @Mock
    UserService userService;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    DashboardController controller;

    @Test
    void testGetHomepage() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());

        when(userService.getById(user.getId())).thenReturn(user);
        when(notificationService.getNotificationsByUser(user.getId())).thenReturn(List.of());

        ModelAndView mav = controller.getHomepage(details);

        assertEquals("home", mav.getViewName());
        assertEquals(user, mav.getModel().get("user"));
        assertEquals(true, mav.getModel().get("logged"));
    }

    @Test
    void testEditProfileGet() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "testUser",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User user = new User();
        user.setId(details.getId());
        user.setUsername("test");
        user.setAvatar("/img/a.png");

        when(userService.getById(details.getId())).thenReturn(user);

        ModelAndView mav = controller.editProfile(details);

        assertEquals("profile_edit", mav.getViewName());
        assertEquals("Home", mav.getModel().get("title"));
    }

    @Test
    void testChangePasswordPost_InvalidPasswords() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setPassword("a");
        req.setRepeat_password("b");

        BindingResult br = new BeanPropertyBindingResult(req, "user");

        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "t",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User user = new User();
        user.setId(details.getId());
        when(userService.getById(details.getId())).thenReturn(user);

        ModelAndView mav = controller.changePassword(details, req, br);

        assertEquals("profile_change_password", mav.getViewName());
        assertTrue(br.hasErrors());
    }

    @Test
    void testPostChangePassword_Success() {
        UUID id = UUID.randomUUID();
        AuthenticationDetails details = new AuthenticationDetails(
                id, "test", "123", UserRole.USER, BigDecimal.ZERO, false
        );

        User user = new User();
        user.setId(id);
        user.setUsername("john");

        when(userService.getById(id)).thenReturn(user);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setPassword("abc123");
        req.setRepeat_password("abc123");

        BindingResult br = new BeanPropertyBindingResult(req, "user");

        ModelAndView mv = controller.changePassword(details, req, br);

        assertEquals("redirect:/dashboard", mv.getViewName());

        verify(userService).changePassword(id, req);
    }

    @Test
    void testGetChangePassword() {
        UUID id = UUID.randomUUID();
        AuthenticationDetails details = new AuthenticationDetails(
                id, "test", "123", UserRole.USER, BigDecimal.ZERO, false
        );

        User user = new User();
        user.setId(id);
        user.setAvatar("/path/avatar.png");

        when(userService.getById(id)).thenReturn(user);

        ModelAndView mv = controller.changePassword(details);

        assertEquals("profile_change_password", mv.getViewName());
        assertInstanceOf(ChangePasswordRequest.class, mv.getModel().get("user"));
        assertEquals("/path/avatar.png", mv.getModel().get("avatar"));
        assertEquals("home", mv.getModel().get("page"));
        assertEquals("Home", mv.getModel().get("title"));
    }

    @Test
    void editProfile_Success_WithAvatarUpload() throws Exception {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(
                id, "oldUser", "123", UserRole.USER, BigDecimal.ZERO, false
        );

        User user = new User();
        user.setId(id);
        user.setUsername("oldUser");

        EditProfileRequest req = new EditProfileRequest();
        req.setUsername("newUser");

        MultipartFile avatar = mock(MultipartFile.class);
        req.setAvatar(avatar);

        when(userService.getById(id)).thenReturn(user);
        when(userService.findByUsername(user.getId(), "newUser")).thenReturn(false);

        when(avatar.isEmpty()).thenReturn(false);
        when(avatar.getOriginalFilename()).thenReturn("имя.png");
        when(avatar.getBytes()).thenReturn("content".getBytes());

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        ModelAndView mv = controller.editProfile(details, req, br);

        assertEquals("redirect:/dashboard", mv.getViewName());
        verify(userService).edit(eq(id), eq(req), anyString());
    }

    @Test
    void editProfile_Success_NoAvatar() throws Exception {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "john", "1", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(id);
        user.setUsername("john");

        EditProfileRequest req = new EditProfileRequest();
        req.setUsername("johnny");
        req.setAvatar(null);

        when(userService.getById(id)).thenReturn(user);
        when(userService.findByUsername(user.getId(), "johnny")).thenReturn(false);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        ModelAndView mv = controller.editProfile(details, req, br);

        assertEquals("redirect:/dashboard", mv.getViewName());
        verify(userService).edit(id, req, null);
    }

    @Test
    void editProfile_Success_WhenAvatarIsEmpty() throws Exception {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "john", "1", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(id);

        EditProfileRequest req = new EditProfileRequest();
        req.setUsername("newName");

        MultipartFile avatar = mock(MultipartFile.class);
        when(avatar.isEmpty()).thenReturn(true);

        req.setAvatar(avatar);

        when(userService.getById(id)).thenReturn(user);
        when(userService.findByUsername(user.getId(), "newName")).thenReturn(false);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        ModelAndView mv = controller.editProfile(details, req, br);

        assertEquals("redirect:/dashboard", mv.getViewName());
        verify(userService).edit(id, req, null);
    }

    @Test
    void editProfile_Fails_WhenUsernameAlreadyExists() throws Exception {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "old", "1", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(id);
        user.setUsername("old");
        user.setAvatar("/avatar.png");

        EditProfileRequest req = new EditProfileRequest();
        req.setUsername("taken");

        BindingResult br = new BeanPropertyBindingResult(req, "user");

        when(userService.getById(id)).thenReturn(user);
        when(userService.findByUsername(id, "taken")).thenReturn(true); // force duplicate username

        ModelAndView mv = controller.editProfile(details, req, br);

        assertEquals("profile_edit", mv.getViewName());
        assertEquals(req, mv.getModel().get("game"));
        assertTrue(br.hasErrors());

        verify(userService, never()).edit(any(), any(), any());
    }

    @Test
    void editProfile_Fails_WithFormErrors() throws Exception {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "john", "1", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(id);
        user.setUsername("john");
        user.setAvatar("/avatar.png");

        EditProfileRequest req = new EditProfileRequest();
        req.setUsername("");

        BindingResult br = new BeanPropertyBindingResult(req, "user");
        br.rejectValue("username", "invalid", "Invalid username");

        when(userService.getById(id)).thenReturn(user);
        when(userService.findByUsername(id, "")).thenReturn(false);

        ModelAndView mv = controller.editProfile(details, req, br);

        assertEquals("profile_edit", mv.getViewName());
        assertEquals(req, mv.getModel().get("game"));
        assertTrue(br.hasErrors());

        verify(userService, never()).edit(any(), any(), any());
    }
}
