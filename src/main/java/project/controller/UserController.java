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

    @GetMapping("/fejlunderoprettelse")
    public String fejl() {
        return "fejlunderoprettelse";
    }

    @PostMapping("/signup")
    public String signup(WebRequest request, Model model) {
        //Undersøger om parametre er null eller tomme
        Iterator<String> paramNames = request.getParameterNames();
        while (paramNames.hasNext()) {
            String paramName = paramNames.next();
            String paramValue = request.getParameter(paramName);
            if (paramValue == null || paramValue.isEmpty()) {
                model.addAttribute("errorMessage", "Felterne må ikke være tomme");
                return "fejlunderoprettelse";
            }
        }
        User newUser = new User();
        newUser.setUsername(request.getParameter("username"));
        newUser.setPassword(request.getParameter("password"));
        newUser.setFirstName(request.getParameter("firstname"));
        newUser.setLastName(request.getParameter("lastname"));

        //Undersøger om brugernavn eller kodeord er for kort.
        if (newUser.getUsername().length() < 3 || newUser.getPassword().length() < 3){
            model.addAttribute("errorMessage", "Brugernavn og kodeord skal være på minimum 3 tegn");
            return "fejlunderoprettelse";
        } else {
            if (userRepo.checkIfDup(newUser)) {
                model.addAttribute("errorMessage", "Brugernavnet er taget");
                return "fejlunderoprettelse";
            } else {
                userRepo.addUser(newUser);
                return "redirect:/";
            }
        }
    }


    //Login og vis brugerens projekter
    @PostMapping("/login")
    public String login(WebRequest request, HttpSession session) {
        User userLogin = new User();
        userLogin.setUsername(request.getParameter("username"));
        userLogin.setPassword(request.getParameter("password"));
        if (userRepo.verifyLogin(userLogin)) {
            session.setAttribute("userlogin", userLogin);
            return "redirect:/projekter";
        } else {
            return "login";
        }
    }

    @GetMapping("/projekter")
    public String projects(Model model, HttpSession session) {
        User userLogin = (User) session.getAttribute("userlogin");
        List<Project> projectList = new ArrayList<>(userRepo.getProjects(userLogin));
        model.addAttribute("projects", projectList);
        session.setAttribute("username", userLogin.getUsername());
        session.setAttribute("userId", userRepo.getUserId(userLogin));
        return "projekter";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }







}
