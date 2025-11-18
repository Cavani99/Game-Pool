package main.web;

import main.model.User;
import main.security.AuthenticationDetails;
import main.service.NotificationService;
import main.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/friends")
public class FriendsController {

    private final UserService userService;

    private final NotificationService notificationService;

    public FriendsController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
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
                                                 @AuthenticationPrincipal AuthenticationDetails userDetails) {
        User user = userService.getById(userDetails.getId());
        notificationService.createFriendInvite(user, userId);

        return Map.of("message", "Friend invitation sent successfully!");
    }

    @GetMapping("accept_request/{id}")
    public ModelAndView acceptFriendRequest(@PathVariable("id") UUID userId, @AuthenticationPrincipal AuthenticationDetails userDetails) {
        User user = userService.getById(userDetails.getId());
        userService.addFriend(user.getId(), userId);

        return new ModelAndView("redirect:/dashboard/friends");
    }

    @GetMapping("remove/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView removeFriend(@PathVariable("id") UUID friendId, @AuthenticationPrincipal AuthenticationDetails userDetails) {
        User user = userService.getById(userDetails.getId());
        userService.removeFriend(user.getId(), friendId);

        return new ModelAndView("redirect:/dashboard/friends");
    }
}
