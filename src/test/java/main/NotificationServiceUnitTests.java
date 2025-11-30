package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import project.model.*;
import project.service.GameService;
import project.service.NotificationService;
import project.service.UserService;
import project.utils.NotificationClient;
import project.utils.client_dtos.CreateNotificationRequest;
import project.utils.client_dtos.CreateUserRequest;
import project.utils.client_dtos.NotificationObject;
import project.utils.client_dtos.NotificationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTests {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private UserService userService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void whenSaveUser_thenClientIsCalled() {
        UUID userId = UUID.randomUUID();
        String username = "Ivan";

        notificationService.saveUser(userId, username);

        ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);
        verify(notificationClient).saveUser(captor.capture());

        CreateUserRequest request = captor.getValue();
        assertEquals(userId, request.getId());
        assertEquals(username, request.getUsername());
    }

    @Test
    void whenCreateFriendInvite_thenClientIsCalled() {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("Ivan");

        notificationService.createFriendInvite(user, friendId);

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationClient).saveNotification(captor.capture());

        CreateNotificationRequest request = captor.getValue();
        assertEquals("Friend Invitation!", request.getTitle());
        assertEquals("You got a friend invitation from Ivan!", request.getMessage());
        assertEquals(NotificationType.REQUEST, request.getType());
        assertEquals("localhost:8080/dashboard/friends/accept_request/" + userId, request.getLink());
        assertEquals("Accept", request.getLinkTitle());
        assertEquals(userId, request.getSenderId());
        assertEquals(friendId, request.getReceiverId());
    }

    @Test
    void whenGameHasPromoPrice_thenNotificationsAreCreated() {
        Discount discount = new Discount();
        discount.setType(DiscountType.PERCENT);
        discount.setAmount(10);
        discount.setEndDate(LocalDateTime.now().plusDays(10));

        UUID gameId = UUID.randomUUID();
        Game game = new Game();
        game.setId(gameId);
        game.setTitle("Game1");
        game.setPrice(60.0);
        game.setDiscount(discount);

        UUID gameId2 = UUID.randomUUID();
        Game game2 = new Game();
        game2.setId(gameId2);
        game2.setTitle("Game2");
        game2.setPrice(60.0);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Martin");

        when(userService.findAllWishlistedUsersByGameId(gameId)).thenReturn(List.of(user));

        notificationService.createGameDiscountNotifications(List.of(game, game2));

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationClient).saveNotification(captor.capture());

        CreateNotificationRequest request = captor.getValue();
        assertEquals("Wishlisted Game got discounted!", request.getTitle());
        assertEquals("Game Game1 got a discount!", request.getMessage());
        assertEquals(NotificationType.INFORMATION, request.getType());
        assertEquals("localhost:8080/dashboard/games/details/" + gameId, request.getLink());
        assertEquals("See Game", request.getLinkTitle());
        assertEquals(user.getId(), request.getReceiverId());
    }

    @Test
    void whenGetNotificationsByUser_thenMappedCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        NotificationResponse response = new NotificationResponse();
        response.setId(UUID.randomUUID());
        response.setSender(senderId);

        NotificationResponse response2 = new NotificationResponse();
        response2.setId(UUID.randomUUID());

        User sender = new User();
        sender.setUsername("Ivan");
        when(notificationClient.getNotifications(userId)).thenReturn(List.of(response, response2));
        when(userService.getById(senderId)).thenReturn(sender);

        List<NotificationObject> result = notificationService.getNotificationsByUser(userId);

        assertEquals(2, result.size());
        assertEquals("Ivan", result.get(0).getSenderUsername());
        assertNull(result.get(1).getSenderUsername());
    }


    @Test
    void whenGetNotificationById_thenClientCalled() {
        UUID id = UUID.randomUUID();
        NotificationResponse response = new NotificationResponse();
        when(notificationClient.getNotification(id)).thenReturn(response);

        NotificationResponse result = notificationService.getNotificationById(id);

        assertSame(response, result);
        verify(notificationClient).getNotification(id);
    }

    @Test
    void whenRemoveNotification_thenClientCalled() {
        UUID id = UUID.randomUUID();
        ResponseEntity<Void> response = ResponseEntity.ok().build();
        when(notificationClient.removeNotification(id)).thenReturn(response);

        HttpStatusCode status = notificationService.removeNotification(id);

        assertEquals(response.getStatusCode(), status);
        verify(notificationClient).removeNotification(id);
    }

    @Test
    void whenRemoveExpiredGameDiscountNotifications_thenExpiredRemoved() {
        UUID gameId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationResponse expiredNotification = new NotificationResponse();
        expiredNotification.setId(notificationId);
        expiredNotification.setLink("localhost:8080/dashboard/games/details/" + gameId);

        UUID gameId2 = UUID.randomUUID();
        UUID notificationId2 = UUID.randomUUID();
        NotificationResponse activeNotification = new NotificationResponse();
        activeNotification.setId(UUID.randomUUID());
        activeNotification.setLink("localhost:8080/dashboard/games/details/" + gameId2);

        Discount discount = new Discount();
        discount.setEndDate(LocalDateTime.now().minusDays(1));

        Game game = new Game();
        game.setDiscount(discount);

        discount = new Discount();
        discount.setType(DiscountType.PERCENT);
        discount.setAmount(10);
        discount.setEndDate(LocalDateTime.now().plusDays(2));

        Game game2 = new Game();
        game2.setPrice(20.0);
        game2.setDiscount(discount);

        when(notificationClient.getGameDiscountNotifications("Wishlisted Game got discounted!"))
                .thenReturn(List.of(expiredNotification, activeNotification));
        when(gameService.findById(gameId)).thenReturn(game);
        when(gameService.findById(gameId2)).thenReturn(game2);

        notificationService.removeExpiredGameDiscountNotifications();

        verify(notificationClient).removeNotification(notificationId);
        verify(notificationClient, never()).removeNotification(notificationId2);
    }

}


