package main.web;

import main.model.User;
import main.security.AuthenticationDetails;
import main.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard/friends")
public class FriendsController {

    private final UserService userService;

    public FriendsController(UserService userService) {
        this.userService = userService;
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
}
