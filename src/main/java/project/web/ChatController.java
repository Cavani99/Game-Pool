package project.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import project.event.payloads.NotificationMessage;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.NotificationService;
import project.service.UserService;

import java.util.*;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final UserService userService;

    private final MessageService messageService;

    private final NotificationService notificationService;

    public ChatController(UserService userService, MessageService messageService, NotificationService notificationService) {
        this.userService = userService;
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getChatMainView(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("chat-home");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("friends", user.getFriends());
        modelAndView.addObject("currentUser", userDetails.getId());
        modelAndView.addObject("page", "chat");
        modelAndView.addObject("title", "Chat");

        return modelAndView;
    }

    @PostMapping("open_chat/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('USER')")
    public Map<String, Object> getChatForUser(@PathVariable("id") UUID friendId, @AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        User user = userService.getById(userDetails.getId());
        User friend = userService.getById(friendId);

        if (!user.getFriends().contains(friend)) {
            String message = messageService.getLocalizedMessage("chat_not_friend", locale);
            return Map.of("error", message);
        }

        //get messages
        List<NotificationMessage> chatMessages = notificationService.getChatNotifications(user.getId(), friendId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userDetails.getId());
        result.put("chat", chatMessages);
        result.put("message", "success");

        return result;
    }
}
