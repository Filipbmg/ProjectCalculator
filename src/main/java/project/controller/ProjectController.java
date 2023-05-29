package project.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import project.model.*;
import org.springframework.ui.Model;
import project.repository.ProjectRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import project.repository.UserRepository;
import project.utility.CalculationTools;

@Controller
public class ProjectController {
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    public ProjectController(ProjectRepository projectRepo, UserRepository userRepo){
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/nytprojekt")
    public String createNew(){
        return "nytprojekt";
    }


    @GetMapping("/projektdetaljer/{projectId}/{ownerId}")
    public String getProjectDetails(@PathVariable("projectId") int projectId, @PathVariable("ownerId") int ownerId, HttpSession session) {
        session.setAttribute("projectId", projectId);
        session.setAttribute("projectowner", ownerId);
        return "redirect:/projekt";
    }

    @GetMapping("/projekt")
    public String project(HttpSession session, Model model) {
        Project project = projectRepo.getProject((int) session.getAttribute("projectId"));
        List<Task> taskList = new ArrayList<>(projectRepo.getTasks((int) session.getAttribute("projectId")));
        List<SubProject> subprojectList = new ArrayList<>(projectRepo.getSubProjects((int) session.getAttribute("projectId")));
        model.addAttribute("project", project);
        model.addAttribute("tasks", taskList);
        model.addAttribute("subprojects", subprojectList);
        model.addAttribute("hoursperday", CalculationTools.projectHoursPerDay(project.getTimeEstimate(), project.getStart(), project.getDeadline()));
        return "projekt";
    }

    @PostMapping("/nytprojekt")
    public String createProject(@RequestParam("projectName") String projectName,
                                @RequestParam("start") String start,
                                @RequestParam("deadline") String deadline,
                                @RequestParam("description") String description,
                                HttpSession session) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            Project newproject = new Project();
            LocalDate startDate = LocalDate.parse(start, format);
            LocalDate deadlineDate = LocalDate.parse(deadline, format);
            newproject.setOwnerId((int) session.getAttribute("userId"));
            newproject.setProjectName(projectName);
            newproject.setStart(startDate);
            newproject.setDeadline(deadlineDate);
            newproject.setProjectDescription(description);
            projectRepo.dbAddProject(newproject);
        } catch (DateTimeParseException e){
            e.printStackTrace();
            return "redirect:/nytprojekt";
        }
        return "redirect:/projekter";
    }

    @GetMapping("/administrerprojekt")
    public String adminProject(HttpSession session, Model model) {
        List<User> userList = userRepo.getUserList((int) session.getAttribute("projectId"));
        model.addAttribute("userlist", userList);
        return "administrerprojekt";
    }

    //Fjerner bruger fra projekt brugerliste
    @PostMapping("/fjernbruger/{userId}")
    public String removeUserFromProject(@PathVariable("userId") int userId, HttpSession session){
        projectRepo.removeUser(userId, (int) session.getAttribute("projectId"));
        return "redirect:/administrerprojekt";
    }

    @PostMapping("/tilfojbruger")
    public String addUserToUserList(WebRequest request, HttpSession session) {
        if (request != null) {
            int userId = userRepo.getUserId(request.getParameter("username"));
            projectRepo.addUserToProject(userId, (int) session.getAttribute("projectId"));
        }
        return "redirect:/administrerprojekt";
    }

    @PostMapping("/edittask")
    public String editTask(@RequestParam("taskId") int taskId, Model model) {
        model.addAttribute("task", projectRepo.getTask(taskId));
        return "redigeropgave";
    }


    @PostMapping("/updatetask/{taskId}")
    public String updateTask(@PathVariable("taskId") int taskId, @ModelAttribute("task") Task updatedTask) {
        Task existingTask = projectRepo.getTask(taskId);
        existingTask.setTimeEstimate(updatedTask.getTimeEstimate());
        existingTask.setTaskDescription(updatedTask.getTaskDescription());
        projectRepo.updateTask(existingTask);
        return "redirect:/ejer";
    }

    @PostMapping("/deletetask/{taskId}")
    public String deleteTask(@PathVariable("taskId") int taskId) {
        projectRepo.deleteTask(taskId);
        return "redirect:/ejer";
    }

    @GetMapping("/delprojekt/{subProjectId}")
    public String subProjectDetails(@PathVariable("subProjectId") int subProjectId, HttpSession session) {
        session.setAttribute("subprojectid", subProjectId);
        return "redirect:/delprojekt";
    }

    @GetMapping("/delprojekt")
    public String delprojekt(HttpSession session, Model model) {
        SubProject subproject = projectRepo.getSubProject((int) session.getAttribute("subprojectid"));
        model.addAttribute("subproject", subproject);
        model.addAttribute("subtasks", projectRepo.getSubTasks(subproject.getSubProjectId()));
        model.addAttribute("hoursperday", CalculationTools.projectHoursPerDay(
                subproject.getTimeEstimate(),
                subproject.getStart(),
                subproject.getDeadline()));
        return "delprojekt";
    }

    @PostMapping("/editsubtask")
    public String editSubTask(@RequestParam("subTaskId") int subTaskId, Model model) {
            model.addAttribute("subtask", projectRepo.getSubTask(subTaskId));
            return "redigerdelopgave";
    }

    @PostMapping("/updatesubtask/{subTaskId}")
    public String updateSubTask(@PathVariable("subTaskId") int subTaskId, @ModelAttribute("task") SubTask updatedTask) {
        SubTask existingSubTask = projectRepo.getSubTask(subTaskId);
        existingSubTask.setTimeEstimate(updatedTask.getTimeEstimate());
        existingSubTask.setSubTaskDescription(updatedTask.getSubTaskDescription());
        projectRepo.updateSubTask(existingSubTask);
        return "redirect:/ejer";
    }

    @PostMapping("/deletesubtask/{subTaskId}")
    public String deleteSubTask(@PathVariable("subTaskId") int subTaskId) {
        projectRepo.deleteSubTask(subTaskId);
        return "redirect:/ejer";
    }



}
