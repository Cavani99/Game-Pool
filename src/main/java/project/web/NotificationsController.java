package project.web;

import project.model.Game;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.GameService;
import project.service.NotificationService;
import project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/notifications")
public class NotificationsController {

    private final UserService userService;
    private final GameService gameService;
    private final NotificationService notificationService;
    private final Logger logger;


    public NotificationsController(UserService userService, GameService gameService, NotificationService notificationService) {
        this.userService = userService;
        this.gameService = gameService;
        this.notificationService = notificationService;
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
    public ModelAndView removeNotification(@PathVariable("id") UUID id) {
        HttpStatusCode status = notificationService.removeNotification(id);
        logger.info("Notification with id {} deleted", id);

        //log status later

        return new ModelAndView("redirect:/dashboard/notifications");
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
