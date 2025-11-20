package main.web;

import main.model.User;
import main.security.AuthenticationDetails;
import main.service.NotificationService;
import main.service.UserService;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/dashboard/notifications")
public class NotificationsController {

    private final UserService userService;
    private final NotificationService notificationService;

    public NotificationsController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
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

        //log status later

        return new ModelAndView("redirect:/dashboard/notifications");
    }
}
