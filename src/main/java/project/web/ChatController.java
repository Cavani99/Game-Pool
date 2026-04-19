package project.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import project.model.User;
import project.security.AuthenticationDetails;
import project.service.MessageService;
import project.service.UserService;

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
        modelAndView.addObject("page", "friends");
        modelAndView.addObject("title", "Friends");

        return modelAndView;
    }
}
