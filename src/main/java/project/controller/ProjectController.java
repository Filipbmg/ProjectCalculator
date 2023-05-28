package project.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import project.model.*;
import org.springframework.ui.Model;
import project.repository.ProjectRepository;
import project.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import project.utility.CalculationTools;

@Controller
public class ProjectController {
    private final ProjectRepository projectRepo;
    public ProjectController(ProjectRepository projectRepo){
        this.projectRepo = projectRepo;
    }

    @GetMapping("/nytprojekt")
    public String createNew(){
        return "nytprojekt";
    }

    @GetMapping("/ejer")
    public String owner(HttpSession session, Model model) {
        Project project = projectRepo.getProject((int) session.getAttribute("projectId"));
        List<Task> taskList = new ArrayList<>(projectRepo.getTasks((int) session.getAttribute("projectId")));
        List<SubProject> subprojectList = new ArrayList<>(projectRepo.getSubProjects((int) session.getAttribute("projectId")));
        model.addAttribute("project", project);
        model.addAttribute("tasks", taskList);
        model.addAttribute("subprojects", subprojectList);
        model.addAttribute("hoursperday", CalculationTools.projectHoursPerDay(project.getTimeEstimate(), project.getDeadline()));
        return "ejer";
    }


    @GetMapping("/projektdetaljer/{projectId}/{ownerId}")
    public String projectDetails(@PathVariable("projectId") int projectId, @PathVariable("ownerId") int ownerId, HttpSession session) {
        int userId = (int) session.getAttribute("userId");
        session.setAttribute("projectId", projectId);
        if (userId == ownerId) {
            return "redirect:/ejer";
        } else {
            return "medlem";
        }
    }

    @PostMapping("/newproject")
    public String createProject(@RequestParam("projectName") String projectName,
                                @RequestParam("deadline") String deadline,
                                HttpSession session) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate deadlineDate = LocalDate.parse(deadline, format);
            Project newproject = new Project();
            newproject.setProjectName(projectName);
            newproject.setOwnerId((int) session.getAttribute("userId"));
            newproject.setDeadline(deadlineDate);
            projectRepo.dbAddProject(newproject);
        } catch (DateTimeParseException e){
            e.printStackTrace();
            return "redirect:/nytprojekt";
        }
        return "redirect:/projekter";
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
        model.addAttribute("hoursperday", CalculationTools.subprojectHoursPerDay(
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
