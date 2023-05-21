package project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import project.model.User;
import project.repository.UserRepository;

import java.util.Iterator;

@Controller
public class UserController {

    private final UserRepository userRepo;
    public UserController(UserRepository userRepo){
        this.userRepo = userRepo;
    }

    @GetMapping({"/", "login"})
    public String front() {
        return "login";
    }

    @GetMapping("/nybruger")
    public String newUser() {
        return "nybruger";
    }

    @GetMapping("/projekter")
    public String projects() {
        return "projekter";
    }

    @GetMapping("/fejlunderoprettelse")
    public String fejl() {
        return "fejlunderoprettelse";
    }

    @PostMapping("/signup")
    public String signup(WebRequest request) {
        //Undersøger om parametre er null eller tomme
        Iterator<String> paramNames = request.getParameterNames();
        while (paramNames.hasNext()) {
            String paramName = paramNames.next();
            String paramValue = request.getParameter(paramName);
            if (paramValue == null || paramValue.isEmpty()) {
                System.out.println("Parameter " + paramName + " is null or empty.");
                return "redirect:/fejlunderoprettelse";
            }
        }

        User newUser = new User();
        newUser.setUsername(request.getParameter("username"));
        newUser.setPassword(request.getParameter("password"));
        newUser.setFirstName(request.getParameter("firstname"));
        newUser.setLastName(request.getParameter("lastname"));

        //Undersøger om brugernavn eller kodeord er for kort.
        if (newUser.getUsername().length() < 3 || newUser.getPassword().length() < 3){
            System.out.println("Password or username too short");
            return "redirect:/fejlunderoprettelse";
        } else {
            if (userRepo.checkIfDup(newUser)) {
                System.out.println("Username taken");
                return "redirect:/fejlunderoprettelse";
            } else {
                userRepo.addUser(newUser);
                return "redirect:/";
            }
        }
    }

    @PostMapping("/login")
    public String login(WebRequest request) {
        User userLogin = new User();
        userLogin.setUsername(request.getParameter("username"));
        userLogin.setPassword(request.getParameter("password"));
        if (userRepo.verifyLogin(userLogin)) {
            return "redirect:/projekter";
        } else {
            return "login";
        }
    }


}
