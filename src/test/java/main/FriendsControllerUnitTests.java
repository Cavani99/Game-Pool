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
import project.service.NotificationService;
import project.service.UserService;
import project.web.FriendsController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendsControllerUnitTests {

    @Mock
    UserService userService;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    FriendsController controller;

    @Test
    void testGetFriendsView() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());
        user.setFriends(List.of());

        when(userService.getById(details.getId())).thenReturn(user);

        ModelAndView mav = controller.getFriendsView(details);

        assertEquals("friends-home", mav.getViewName());
        assertEquals(List.of(), mav.getModel().get("friends"));
    }

    @Test
    void testGetUserDetails() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);

        when(userService.getById(id)).thenReturn(user);

        ModelAndView mv = controller.getUserDetails(id);

        assertEquals("home", mv.getViewName());
        assertEquals(user, mv.getModel().get("user"));
        assertEquals("friends", mv.getModel().get("page"));
        assertEquals("Friends", mv.getModel().get("title"));
    }

    @Test
    void testSearchUsers() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User user = new User();
        user.setId(details.getId());
        user.setFriends(List.of());

        User other = new User();
        other.setId(UUID.randomUUID());
        other.setUsername("Mark");

        when(userService.getById(details.getId())).thenReturn(user);
        when(userService.findAllUsers()).thenReturn(List.of(user, other));

        Map<String, String> req = Map.of("search", "ma");

        ModelAndView mav = controller.searchUsers(details, req);

        List<User> users = (List<User>) mav.getModel().get("users");
        assertEquals(1, users.size());
        assertEquals(other, users.get(0));
    }

    @Test
    void testSendFriendRequest() {
        UUID receiverId = UUID.randomUUID();
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);

        User sender = new User();
        sender.setId(details.getId());

        User friend = new User();
        friend.setId(UUID.randomUUID());
        friend.setUsername("Friend");

        when(userService.getById(details.getId())).thenReturn(sender);
        when(userService.getById(receiverId)).thenReturn(friend);

        Locale locale = Locale.getDefault();
        Map<String, String> result = controller.sendFriendRequest(receiverId, details, locale);

        assertEquals("Friend invitation sent successfully!", result.get("message"));
        verify(notificationService).createFriendInvite(sender, receiverId);
    }

    @Test
    void testAcceptFriendRequest() {
        UUID currentUser = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        AuthenticationDetails auth = new AuthenticationDetails(currentUser, "test",
                "12", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(currentUser);

        User friend = new User();
        friend.setId(friendId);
        friend.setUsername("Mark");

        when(userService.getById(currentUser)).thenReturn(user);
        when(userService.userNotFriend(currentUser, friendId)).thenReturn(true);
        when(userService.getById(friendId)).thenReturn(friend);

        Locale locale = Locale.getDefault();
        ModelAndView mv = controller.acceptFriendRequest(friendId, auth, locale);

        assertEquals("redirect:/dashboard/friends", mv.getViewName());
        verify(userService).addFriend(currentUser, friendId);
    }

    @Test
    void testAcceptFriendRequestForUserThatIsAlreadyFriend() {
        UUID currentUser = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        AuthenticationDetails auth = new AuthenticationDetails(currentUser, "test",
                "12", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(currentUser);

        User friend = new User();
        friend.setId(friendId);
        friend.setUsername("Mark");

        when(userService.getById(currentUser)).thenReturn(user);
        when(userService.userNotFriend(currentUser, friendId)).thenReturn(false);

        Locale locale = Locale.getDefault();
        ModelAndView mv = controller.acceptFriendRequest(friendId, auth, locale);

        assertEquals("redirect:/dashboard/friends", mv.getViewName());
        verify(userService, never()).addFriend(any(), any());
        verify(userService, times(1)).getById(any());
    }

    @Test
    void testSearchUsers_UserIsFriend_Excluded() {
        UUID id = UUID.randomUUID();
        AuthenticationDetails details = new AuthenticationDetails(id, "test",
                "12", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(id);

        User friend = new User();
        friend.setId(UUID.randomUUID());
        friend.setUsername("Mark");

        user.setFriends(List.of(friend));

        when(userService.getById(id)).thenReturn(user);
        when(userService.findAllUsers()).thenReturn(List.of(user, friend));

        Map<String, String> req = Map.of("search", "ma");

        ModelAndView mv = controller.searchUsers(details, req);

        List<User> result = (List<User>) mv.getModel().get("users");

        assertTrue(result.isEmpty());
    }

    @Test
    void testAcceptFriendRequestWithNotification() {
        UUID currentId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        UUID notifyId = UUID.randomUUID();

        AuthenticationDetails auth = new AuthenticationDetails(currentId, "test",
                "12", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(currentId);

        User friend = new User();
        friend.setId(friendId);
        friend.setUsername("Alice");

        when(userService.getById(currentId)).thenReturn(user);
        when(userService.userNotFriend(currentId, friendId)).thenReturn(true);
        when(userService.getById(friendId)).thenReturn(friend);

        Locale locale = Locale.getDefault();
        ModelAndView mv = controller.acceptFriendRequest(friendId, notifyId, auth, locale);

        assertEquals("redirect:/dashboard/notifications/remove/" + notifyId, mv.getViewName());
        verify(userService).addFriend(currentId, friendId);
    }

    @Test
    void testAcceptFriendRequestForUserThatIsAlreadyFriendWithNotification() {
        UUID currentUser = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        UUID notifyId = UUID.randomUUID();

        AuthenticationDetails auth = new AuthenticationDetails(currentUser, "test",
                "12", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(currentUser);

        User friend = new User();
        friend.setId(friendId);
        friend.setUsername("Mark");

        when(userService.getById(currentUser)).thenReturn(user);
        when(userService.userNotFriend(currentUser, friendId)).thenReturn(false);

        Locale locale = Locale.getDefault();
        ModelAndView mv = controller.acceptFriendRequest(friendId, notifyId, auth, locale);

        assertEquals("redirect:/dashboard/notifications/remove/" + notifyId, mv.getViewName());
        verify(userService, never()).addFriend(any(), any());
        verify(userService, times(1)).getById(any());
    }

    @Test
    void testRemoveFriend() {
        UUID currentId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        AuthenticationDetails auth = new AuthenticationDetails(currentId, "test",
                "12", UserRole.USER, BigDecimal.ZERO, false);

        User user = new User();
        user.setId(currentId);

        User friend = new User();
        friend.setId(friendId);

        when(userService.getById(currentId)).thenReturn(user);
        when(userService.getById(friendId)).thenReturn(friend);

        Locale locale = Locale.getDefault();
        ModelAndView mv = controller.removeFriend(friendId, auth, locale);

        assertEquals("redirect:/dashboard/friends", mv.getViewName());
        verify(userService).removeFriend(currentId, friendId);
    }

}
