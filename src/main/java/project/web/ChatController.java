package project.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.UserService;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final UserService userService;

    private final MessageService messageService;

    public ChatController(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView getChatMainView(@AuthenticationPrincipal AuthenticationDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("chat-home");

        User user = userService.getById(userDetails.getId());

        modelAndView.addObject("friends", user.getFriends());
        modelAndView.addObject("page", "chat");
        modelAndView.addObject("title", "Chat");

        return modelAndView;
    }

    @PostMapping("open_chat/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('USER')")
    public Map<String, String> getChatForUser(@PathVariable("id") UUID friendId, @AuthenticationPrincipal AuthenticationDetails userDetails, Locale locale) {
        User user = userService.getById(userDetails.getId());
        User friend = userService.getById(friendId);

        if (!user.getFriends().contains(friend)) {
            String message = messageService.getLocalizedMessage("chat_not_friend", locale);
            return Map.of("error", message);
        }

        //get messages

        return Map.of("message", "success");
    }
}
