package main.web;

import jakarta.validation.Valid;
import main.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
public class IndexController {

    // TODO: Implement the necessary here.
    @GetMapping
    public String index() {
        return "index";
    }
}
