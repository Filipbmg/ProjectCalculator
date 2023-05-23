package project.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import project.model.*;
import org.springframework.ui.Model;
import project.repository.UserRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            return "redirect:/fejlunderoprettelse";
        } else {
            if (userRepo.checkIfDup(newUser)) {
                return "redirect:/fejlunderoprettelse";
            } else {
                userRepo.addUser(newUser);
                return "redirect:/";
            }
        }
    }


    //Login og vis brugerens projekter
    @PostMapping("/projekter")
    public String login(WebRequest request, HttpSession session, Model model) {
        User userLogin = new User();
        userLogin.setUsername(request.getParameter("username"));
        userLogin.setPassword(request.getParameter("password"));
        if (userRepo.verifyLogin(userLogin)) {
            List<Project> projectList = new ArrayList<>(userRepo.fetchProjects(userLogin));
            model.addAttribute("projects", projectList);
            session.setAttribute("username", userLogin.getUsername());
            session.setAttribute("userId", userRepo.getUserId(userLogin));
            return "projekter";
        } else {
            return "login";
        }
    }






}
