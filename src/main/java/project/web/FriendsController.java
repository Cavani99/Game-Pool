package project.web;

import project.model.User;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.NotificationService;
import project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/friends")
public class FriendsController {

    private final UserService userService;

    private final NotificationService notificationService;
    private final Logger logger;

    private final MessageService messageService;

    public FriendsController(UserService userService, NotificationService notificationService, MessageService messageService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.logger = LoggerFactory.getLogger(FriendsController.class);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getFriendsView(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("friends-home");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("friends", user.getFriends());
        modelAndView.addObject("page", "friends");
        modelAndView.addObject("title", "Friends");

        return modelAndView;
    }

    @GetMapping("details/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getUserDetails(@PathVariable("id") UUID id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");

        User user = userService.getById(id);

        modelAndView.addObject("user", user);
        modelAndView.addObject("page", "friends");
        modelAndView.addObject("title", "Friends");

        return modelAndView;
    }

    @PostMapping("search_users")
    public ModelAndView searchUsers(@AuthenticationPrincipal AuthenticationDetails userDetails,
                                    @RequestBody Map<String, String> searchRequest) {
        ModelAndView modelAndView = new ModelAndView("/fragments/add-friend-fragment :: users");

        String searchText = searchRequest.get("search").toLowerCase();

        User user = userService.getById(userDetails.getId());
        List<User> friends = user.getFriends();

        List<User> allUsers = userService.findAllUsers();

        List<User> filteredUsers = allUsers.stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .filter(u -> !friends.contains(u))
                .filter(u -> u.getUsername().toLowerCase().contains(searchText))
                .toList();

        modelAndView.addObject("users", filteredUsers);
        return modelAndView;
    }

    @PostMapping("send_request/{id}")
    @ResponseBody
    public Map<String, String> sendFriendRequest(@PathVariable("id") UUID userId,
                                                 @AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        User user = userService.getById(userDetails.getId());
        notificationService.createFriendInvite(user, userId);

        User friend = userService.getById(userId);
        String message = messageService.getLocalizedMessage("friend_invite", locale);
        logger.info(message, friend.getUsername());

        message = messageService.getLocalizedMessage("friend_invite_success", locale);
        return Map.of("message", message);
    }

    @GetMapping("accept_request/{id}")
    public ModelAndView acceptFriendRequest(@PathVariable("id") UUID userId, @AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        User user = userService.getById(userDetails.getId());

        if (userService.userNotFriend(user.getId(), userId)) {
            userService.addFriend(user.getId(), userId);

            User friend = userService.getById(userId);
            String message = messageService.getLocalizedMessage("friend_added", locale);
            logger.info(message, friend.getUsername(), user.getUsername());
        }

        return new ModelAndView("redirect:/dashboard/friends");
    }

    @GetMapping("accept_request/{id}/{notification_id}")
    public ModelAndView acceptFriendRequest(@PathVariable("id") UUID userId, @PathVariable("notification_id") UUID notificationId,
                                            @AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        User user = userService.getById(userDetails.getId());
        if (userService.userNotFriend(user.getId(), userId)) {
            userService.addFriend(user.getId(), userId);

            User friend = userService.getById(userId);
            String message = messageService.getLocalizedMessage("friend_added", locale);
            logger.info(message, friend.getUsername(), user.getUsername());
        }

        return new ModelAndView("redirect:/dashboard/notifications/remove/" + notificationId);
    }

    @GetMapping("remove/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView removeFriend(@PathVariable("id") UUID friendId, @AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        User user = userService.getById(userDetails.getId());
        userService.removeFriend(user.getId(), friendId);

        User friend = userService.getById(friendId);
        String message = messageService.getLocalizedMessage("friend_remove", locale);
        logger.info(message, friend.getUsername());

        return new ModelAndView("redirect:/dashboard/friends");
    }
}
