package project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@Controller
public class UserController {

    @GetMapping({"/", "login"})
    public String front() {
        return "login";
    }

    @GetMapping("/nybruger")
    public String newUser() {
        return "nybruger";
    }

    @PostMapping("/login")
    public String login() {
        
    }


}
