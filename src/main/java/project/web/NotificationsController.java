package project.web;

import org.springframework.web.bind.annotation.*;
import project.model.Game;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.GameService;
import project.service.MessageService;
import project.service.NotificationService;
import project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/notifications")
public class NotificationsController {

    private final UserService userService;
    private final GameService gameService;
    private final NotificationService notificationService;
    private final Logger logger;
    private final MessageService messageService;


    public NotificationsController(UserService userService, GameService gameService, NotificationService notificationService, MessageService messageService) {
        this.userService = userService;
        this.gameService = gameService;
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.logger = LoggerFactory.getLogger(NotificationsController.class);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView seeNotifications(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("notifications-list");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("notifications", notificationService.getNotificationsByUser(user.getId()));
        modelAndView.addObject("page", "notifications");
        modelAndView.addObject("title", "Notifications");

        return modelAndView;
    }

    @GetMapping("details/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView seeNotification(@PathVariable("id") UUID id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("notification-details");

        modelAndView.addObject("notification", notificationService.getNotificationById(id));
        modelAndView.addObject("page", "notifications");
        modelAndView.addObject("title", "Notifications");

        return modelAndView;
    }

    @GetMapping("remove/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView removeNotification(@PathVariable("id") UUID id, Locale locale) {
        HttpStatusCode status = notificationService.removeNotification(id);

        String message = messageService.getLocalizedMessage("notification_deleted", locale);
        logger.info(message, id, status);

        return new ModelAndView("redirect:/dashboard/notifications");
    }

    @PostMapping("sent/{friend_id}")
    @PreAuthorize("hasAuthority('USER')")
    public void sendChatMessage(@AuthenticationPrincipal AuthenticationDetails userDetails, @PathVariable("friend_id") UUID friendId,
                                @RequestBody Map<String, String> messageRequest) {
        String message = messageRequest.get("message");
        notificationService.createChatMessageRequest(userDetails.getId(), friendId, message);
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void sentDiscountNotifications() {
        List<Game> games = gameService.findAll();

        notificationService.createGameDiscountNotifications(games);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void removeExpiredGameDiscountsNotifications() {
        notificationService.removeExpiredGameDiscountNotifications();
    }
}
