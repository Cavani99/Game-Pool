package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import project.model.Game;
import project.model.User;
import project.model.UserRole;
import project.security.AuthenticationDetails;
import project.service.GameService;
import project.service.MessageService;
import project.service.NotificationService;
import project.service.UserService;
import project.event.payloads.NotificationResponse;
import project.web.NotificationsController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationsControllerUnitTests {

    @Mock
    UserService userService;
    @Mock
    GameService gameService;
    @Mock
    NotificationService notificationService;
    @Mock
    private MessageService messageService;

    @InjectMocks
    NotificationsController controller;

    @Test
    void testSeeNotifications() {
        AuthenticationDetails details = new AuthenticationDetails(UUID.randomUUID(), "test",
                "12", UserRole.USER, BigDecimal.valueOf(10.00), false);
        User user = new User();
        user.setId(details.getId());

        when(userService.getById(details.getId())).thenReturn(user);
        when(notificationService.getNotificationsByUser(user.getId())).thenReturn(List.of());

        ModelAndView mav = controller.seeNotifications(details);

        assertEquals("notifications-list", mav.getViewName());
    }

    @Test
    void testSeeNotificationDetails() {
        UUID nid = UUID.randomUUID();

        NotificationResponse n = new NotificationResponse();
        n.setId(nid);

        when(notificationService.getNotificationById(nid)).thenReturn(n);

        ModelAndView mav = controller.seeNotification(nid);

        assertEquals("notification-details", mav.getViewName());
        assertEquals(n, mav.getModel().get("notification"));
    }

    @Test
    void testRemoveNotification() {
        UUID nid = UUID.randomUUID();

        when(notificationService.removeNotification(nid))
                .thenReturn(HttpStatus.OK);

        Locale locale = Locale.ENGLISH;
        ModelAndView mav = controller.removeNotification(nid, locale);

        assertEquals("redirect:/dashboard/notifications", mav.getViewName());
    }

    @Test
    void sentDiscountNotifications_ShouldCallNotificationService() {
        Game game1 = new Game();
        Game game2 = new Game();
        when(gameService.findAll()).thenReturn(List.of(game1, game2));

        controller.sentDiscountNotifications();

        verify(notificationService).createGameDiscountNotifications(List.of(game1, game2));
    }

    @Test
    void removeExpiredGameDiscountsNotifications_ShouldCallNotificationService() {
        controller.removeExpiredGameDiscountsNotifications();

        verify(notificationService).removeExpiredGameDiscountNotifications();
    }
}

