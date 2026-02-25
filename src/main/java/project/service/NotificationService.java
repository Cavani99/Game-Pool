package project.service;

import project.event.SaveNotificationEventPublisher;
import project.event.UserAddedEventPublisher;
import project.model.Game;
import project.model.NotificationType;
import project.model.User;
import project.utils.NotificationClient;
import project.event.payloads.CreateNotificationRequest;
import project.event.payloads.CreateUserRequest;
import project.event.payloads.NotificationObject;
import project.event.payloads.NotificationResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationClient notificationClient;
    private final UserService userService;

    private final GameService gameService;
    private final UserAddedEventPublisher userAddedEventPublisher;

    private final SaveNotificationEventPublisher saveNotificationEventPublisher;

    public NotificationService(NotificationClient notificationClient, UserService userService, GameService gameService,
                               UserAddedEventPublisher userAddedEventPublisher, SaveNotificationEventPublisher saveNotificationEventPublisher) {
        this.notificationClient = notificationClient;
        this.userService = userService;
        this.gameService = gameService;
        this.userAddedEventPublisher = userAddedEventPublisher;
        this.saveNotificationEventPublisher = saveNotificationEventPublisher;
    }

    public void saveUser(UUID userId, String username) {
        CreateUserRequest createUserRequest = new CreateUserRequest(userId, username);
        userAddedEventPublisher.send(createUserRequest);
    }

    public void createFriendInvite(User user, UUID invitedUserId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("Friend Invitation!");
        request.setMessage("You got a friend invitation from " + user.getUsername() + "!");
        request.setType(NotificationType.REQUEST);
        request.setLink("localhost:8080/dashboard/friends/accept_request/" + user.getId());
        request.setLinkTitle("Accept");
        request.setSenderId(user.getId());
        request.setReceiverId(invitedUserId);
        saveNotificationEventPublisher.send(request);
    }

    public void createGameDiscountNotifications(List<Game> games) {
        for (Game game : games) {
            if (game.getPromoPrice() > 0) {
                List<User> users = userService.findAllWishlistedUsersByGameId(game.getId());
                createGameDiscountNotifications(game, users);
            }
        }
    }

    private void createGameDiscountNotifications(Game game, List<User> users) {
        for (User user : users) {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle("Wishlisted Game got discounted!");
            request.setMessage("Game " + game.getTitle() + " got a discount!");
            request.setType(NotificationType.INFORMATION);
            request.setLink("localhost:8080/dashboard/games/details/" + game.getId());
            request.setLinkTitle("See Game");
            request.setReceiverId(user.getId());
            saveNotificationEventPublisher.send(request);
        }
    }

    public List<NotificationObject> getNotificationsByUser(UUID userId) {
        List<NotificationResponse> notifications = notificationClient.getNotifications(userId);

        return notifications.stream()
                .map(notification -> {
                    if (notification.getSender() != null) {
                        User sender = userService.getById(notification.getSender());
                        return new NotificationObject(notification, sender.getUsername());
                    } else {
                        return new NotificationObject(notification, null);
                    }
                })
                .toList();
    }

    public NotificationResponse getNotificationById(UUID id) {
        return notificationClient.getNotification(id);
    }

    public HttpStatusCode removeNotification(UUID id) {
        ResponseEntity<Void> response = notificationClient.removeNotification(id);

        return response.getStatusCode();
    }

    public void removeExpiredGameDiscountNotifications() {
        List<NotificationResponse> notifications = notificationClient.getGameDiscountNotifications("Wishlisted Game got discounted!");

        for (NotificationResponse notificationResponse : notifications) {
            UUID gameId = UUID.fromString(notificationResponse.getLink().substring(notificationResponse.getLink().lastIndexOf("/") + 1));

            Game game = gameService.findById(gameId);
            if (game.getPromoPrice() <= 0) {
                notificationClient.removeNotification(notificationResponse.getId());
            }
        }
    }
}
