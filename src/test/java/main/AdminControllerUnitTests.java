package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import project.model.User;
import project.model.UserRole;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.UserService;
import project.web.admin.AdminController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

import org.springframework.web.servlet.ModelAndView;
import project.web.dto.EditProfileRequest;


@ExtendWith(MockitoExtension.class)
public class AdminControllerUnitTests {

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

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

    @Test
    void editAdmin_ShouldReturnCorrectModelAndView() {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("AdminUser");
        mockUser.setAvatar("picture");

        EditProfileRequest editProfileRequest = new EditProfileRequest(mockUser.getUsername(), null, mockUser.getAvatar());

        when(userService.getById(id)).thenReturn(mockUser);

        ModelAndView mav = adminController.editAdmin(details, Locale.ENGLISH);

        assertNotNull(mav);
        assertEquals("admin/edit-profile", mav.getViewName());

        EditProfileRequest actual = (EditProfileRequest) mav.getModel().get("user");

        assertEquals(editProfileRequest.getUsername(), actual.getUsername());
        assertEquals(editProfileRequest.getAvatar(), actual.getAvatar());
        assertEquals("profile", mav.getModel().get("page"));
        assertEquals("Profile", mav.getModel().get("title"));

        verify(userService).getById(id);
    }

    @Test
    void editProfile_ShouldRedirectToAdminWhenSuccessful() throws IOException {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("AdminUser");
        mockUser.setAvatar("picture");

        EditProfileRequest editProfileRequest = new EditProfileRequest(mockUser.getUsername(), null, mockUser.getAvatar());
        MultipartFile mockAvatar = mock(MultipartFile.class);
        editProfileRequest.setAvatar(mockAvatar);

        when(mockAvatar.isEmpty()).thenReturn(false);
        when(mockAvatar.getOriginalFilename()).thenReturn("picture.png");
        when(mockAvatar.getBytes()).thenReturn("dummy".getBytes());

        BindingResult bindingResult = mock(BindingResult.class);

        when(userService.getById(id)).thenReturn(mockUser);
        when(userService.findByUsername(id, editProfileRequest.getUsername())).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mav = adminController.editProfile(details, editProfileRequest, bindingResult, Locale.ENGLISH);

        assertNotNull(mav);
        assertEquals("redirect:/admin", mav.getViewName());

        ArgumentCaptor<String> avatarPathCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService).edit(
                eq(id),
                eq(editProfileRequest),
                avatarPathCaptor.capture()
        );

        String actualAvatarPath = avatarPathCaptor.getValue();
        assertTrue(actualAvatarPath.startsWith("/uploads/avatars/"));
        assertTrue(actualAvatarPath.endsWith("_picture.png"));
    }

    @Test
    void editProfileWithNoAvatar_ShouldRedirectToAdmin() throws IOException {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("AdminUser");
        mockUser.setAvatar("picture");

        EditProfileRequest editProfileRequest = new EditProfileRequest(mockUser.getUsername(), null, mockUser.getAvatar());

        BindingResult bindingResult = mock(BindingResult.class);

        when(userService.getById(id)).thenReturn(mockUser);
        when(userService.findByUsername(id, editProfileRequest.getUsername())).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mav = adminController.editProfile(details, editProfileRequest, bindingResult, Locale.ENGLISH);

        assertNotNull(mav);
        assertEquals("redirect:/admin", mav.getViewName());

        ArgumentCaptor<String> avatarPathCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService).edit(
                eq(id),
                eq(editProfileRequest),
                avatarPathCaptor.capture()
        );

        String actualAvatarPath = avatarPathCaptor.getValue();
        assertNull(actualAvatarPath);
    }

    @Test
    void editProfileWithEmptyAvatar_ShouldRedirectToAdmin() throws IOException {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("AdminUser");
        mockUser.setAvatar("picture");

        EditProfileRequest editProfileRequest = new EditProfileRequest(mockUser.getUsername(), null, mockUser.getAvatar());
        MultipartFile mockAvatar = mock(MultipartFile.class);
        editProfileRequest.setAvatar(mockAvatar);

        when(mockAvatar.isEmpty()).thenReturn(true);

        BindingResult bindingResult = mock(BindingResult.class);

        when(userService.getById(id)).thenReturn(mockUser);
        when(userService.findByUsername(id, editProfileRequest.getUsername())).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mav = adminController.editProfile(details, editProfileRequest, bindingResult, Locale.ENGLISH);

        assertNotNull(mav);
        assertEquals("redirect:/admin", mav.getViewName());

        ArgumentCaptor<String> avatarPathCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService).edit(
                eq(id),
                eq(editProfileRequest),
                avatarPathCaptor.capture()
        );

        String actualAvatarPath = avatarPathCaptor.getValue();
        assertNull(actualAvatarPath);
    }

    @Test
    void editProfileFailed_ShouldReturnUsernameUniqueMessage() throws IOException {
        UUID id = UUID.randomUUID();

        AuthenticationDetails details = new AuthenticationDetails(id, "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("AdminUser");
        mockUser.setAvatar("picture");

        EditProfileRequest editProfileRequest = new EditProfileRequest(mockUser.getUsername(), null, mockUser.getAvatar());
        String message = "Please, write unique username!";

        BindingResult bindingResult = mock(BindingResult.class);
        bindingResult.rejectValue("username", "username.empty", message);

        when(userService.getById(id)).thenReturn(mockUser);
        when(userService.findByUsername(id, editProfileRequest.getUsername())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(messageService.getLocalizedMessage("unique_username", Locale.ENGLISH)).thenReturn(message);

        ModelAndView mav = adminController.editProfile(details, editProfileRequest, bindingResult, Locale.ENGLISH);

        assertNotNull(mav);
        assertEquals(messageService.getLocalizedMessage("unique_username", Locale.ENGLISH), message);
        assertEquals("admin/edit-profile", mav.getViewName());
        assertEquals("profile", mav.getModel().get("page"));
        assertEquals("Profile", mav.getModel().get("title"));
    }
}
