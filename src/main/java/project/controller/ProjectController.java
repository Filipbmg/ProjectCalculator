package project.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.model.*;
import org.springframework.ui.Model;
import project.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import project.utility.CalculationTools;

@Controller
public class ProjectController {
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final SubProjectRepository subProjectRepo;
    private final TaskRepository taskRepo;
    private final SubTaskRepository subTaskRepo;
    public ProjectController(ProjectRepository projectRepo,
                             UserRepository userRepo,
                             SubProjectRepository subProjectRepo,
                             TaskRepository taskRepo,
                             SubTaskRepository subTaskRepo){
        this.projectRepo = projectRepo;
        this.subProjectRepo = subProjectRepo;
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
        this.subTaskRepo = subTaskRepo;
    }

    @GetMapping("/nytprojekt")
    public String createNewProject(){
        return "nytprojekt";
    }

    @GetMapping("/nyopgave")
    public String createNewTask() {
        return "nyopgave";
    }

    @GetMapping("/nydelopgave")
    public String createNewSubTask() {
        return "nydelopgave";
    }

    @GetMapping("/nytdelprojekt")
    public String createNewSubProject() {
        return "nytdelprojekt";
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
        List<Task> taskList = new ArrayList<>(taskRepo.getTasks((int) session.getAttribute("projectId")));
        List<SubProject> subprojectList = new ArrayList<>(subProjectRepo.getSubProjects((int) session.getAttribute("projectId")));
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
        //Fejlhåndtering af data input
        if (start.length() != 10 || deadline.length() != 10) {
            return "redirect:/nytprojekt";
        }
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

    @GetMapping("/projektmedlemmer")
    public String adminProject(HttpSession session, Model model) {
        Project project = projectRepo.getProject((int) session.getAttribute("projectId"));
        List<User> userList = userRepo.getUserList((int) session.getAttribute("projectId"));
        model.addAttribute("userlist", userList);
        model.addAttribute("project", project);
        return "Projektmedlemmer";
    }

    //Fjerner bruger fra projekt brugerliste
    @PostMapping("/fjernbruger")
    public String removeUserFromProject(@RequestParam("userId") int userId, HttpSession session){
        projectRepo.removeUser(userId, (int) session.getAttribute("projectId"));
        return "redirect:/projektmedlemmer";
    }

    //Tilføjer bruger til projekt brugerliste
    @PostMapping("/tilføjbruger")
    public String addUserToUserList(@RequestParam("username") String username, HttpSession session) {
        //Undersøg om request = null og om brugeren eksister før brugeren bliver tilføjet til projekt
        if (username != null && userRepo.checkIfUserExists(username)) {
            int userId = userRepo.getUserId(username);
            projectRepo.addUserToProject(userId, (int) session.getAttribute("projectId"));
        }
        return "redirect:/projektmedlemmer";
    }

    //Tager brugeren til en side hvor de kan redigere en projekt-opgave
    @PostMapping("/redigeropgave")
    public String editTask(@RequestParam("taskId") int taskId, Model model) {
        model.addAttribute("task", taskRepo.getTask(taskId));
        return "redigeropgave";
    }

    //Opdaterer projektopgave
    @PostMapping("/opdateropgave")
    public String updateTask(@RequestParam("taskId") int taskId, @ModelAttribute("task") Task updatedTask) {
        Task existingTask = taskRepo.getTask(taskId);
        existingTask.setTimeEstimate(updatedTask.getTimeEstimate());
        existingTask.setTaskDescription(updatedTask.getTaskDescription());
        taskRepo.updateTask(existingTask);
        return "redirect:/projekt";
    }

    @PostMapping("/nyopgave")
    public String newTask(@RequestParam("taskName") String taskName,
                          @RequestParam("description") String description,
                          @RequestParam("start") String start,
                          @RequestParam("deadline") String deadline,
                          @RequestParam("timeestimate") int timeEstimate,
                          HttpSession session) {
        //Fejlhåndtering af datofelt
        if (start.length() != 10 || deadline.length() != 10) {
            return "redirect:/nyopgave";
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            Task newTask = new Task();
            LocalDate startDate = LocalDate.parse(start, format);
            LocalDate deadlineDate = LocalDate.parse(deadline, format);
            newTask.setTaskName(taskName);
            newTask.setTaskDescription(description);
            newTask.setProjectId((int) session.getAttribute("projectId"));
            newTask.setStart(startDate);
            newTask.setDeadline(deadlineDate);
            newTask.setTimeEstimate(timeEstimate);
            taskRepo.createTask(newTask);
        } catch (DateTimeParseException e){
            e.printStackTrace();
            return "redirect:/nyopgave";
        }
        return "redirect:/projekt";
    }

    @PostMapping("/sletopgave")
    public String deleteTask(@RequestParam("taskId") int taskId) {
        taskRepo.deleteTask(taskId);
        return "redirect:/projekt";
    }

    @GetMapping("/delprojekt/{subProjectId}")
    public String subProjectDetails(@PathVariable("subProjectId") int subProjectId, HttpSession session) {
        session.setAttribute("subprojectid", subProjectId);
        return "redirect:/delprojekt";
    }

    @GetMapping("/delprojekt")
    public String delprojekt(HttpSession session, Model model) {
        SubProject subproject = subProjectRepo.getSubProject((int) session.getAttribute("subprojectid"));
        model.addAttribute("subproject", subproject);
        model.addAttribute("subtasks", subTaskRepo.getSubTasks(subproject.getSubProjectId()));
        model.addAttribute("hoursperday", CalculationTools.projectHoursPerDay(
                subproject.getTimeEstimate(),
                subproject.getStart(),
                subproject.getDeadline()));
        return "delprojekt";
    }

    @PostMapping("/redigerdelopgave")
    public String editSubTask(@RequestParam("subTaskId") int subTaskId, Model model) {
            model.addAttribute("subtask", subTaskRepo.getSubTask(subTaskId));
            return "redigerdelopgave";
    }

    @PostMapping("/opdaterdelopgave")
    public String updateSubTask(@RequestParam("subTaskId") int subTaskId, @ModelAttribute("task") SubTask updatedTask) {
        SubTask existingSubTask = subTaskRepo.getSubTask(subTaskId);
        existingSubTask.setTimeEstimate(updatedTask.getTimeEstimate());
        existingSubTask.setSubTaskDescription(updatedTask.getSubTaskDescription());
        subTaskRepo.updateSubTask(existingSubTask);
        return "redirect:/delprojekt";
    }

    @PostMapping("/sletdelopgave")
    public String deleteSubTask(@RequestParam("subTaskId") int subTaskId) {
        subTaskRepo.deleteSubTask(subTaskId);
        return "redirect:/delprojekt";
    }

    @PostMapping("/sletprojekt")
    public String deleteProject(@RequestParam("projectId") int projectId) {
        projectRepo.deleteProject(projectId);
        return "redirect:/projekter";
    }

    @PostMapping("/sletdelprojekt")
    public String deleteSubProject(@RequestParam("subProjectId") int subProjectId) {
        subProjectRepo.deleteSubProject(subProjectId);
        return "redirect:/projekt";
    }

    @PostMapping("/opdaterprojekt")
    public String updateProject(@RequestParam("projectId") int projectId,
                                @ModelAttribute("project") Project updatedProject) {
        Project existingProject = projectRepo.getProject(projectId);
        existingProject.setProjectDescription(updatedProject.getProjectDescription());
        existingProject.setStart(updatedProject.getStart());
        existingProject.setDeadline(updatedProject.getDeadline());
        projectRepo.updateProject(existingProject);
        return "redirect:/projekter";
    }

    @PostMapping("/nydelopgave")
    public String newSubTask(@RequestParam("subTaskName") String subTaskName,
                          @RequestParam("description") String description,
                          @RequestParam("start") String start,
                          @RequestParam("deadline") String deadline,
                          @RequestParam("timeestimate") int timeEstimate,
                          HttpSession session) {
        if (start.length() != 10 || deadline.length() != 10) {
            return "redirect:/nydelopgave";
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            SubTask newSubTask = new SubTask();
            LocalDate startDate = LocalDate.parse(start, format);
            LocalDate deadlineDate = LocalDate.parse(deadline, format);
            newSubTask.setSubTaskName(subTaskName);
            newSubTask.setSubTaskDescription(description);
            newSubTask.setSubProjectId((int) session.getAttribute("subprojectid"));
            newSubTask.setStart(startDate);
            newSubTask.setDeadline(deadlineDate);
            newSubTask.setTimeEstimate(timeEstimate);
            subTaskRepo.createSubTask(newSubTask);
        } catch (DateTimeParseException e){
            e.printStackTrace();
            return "redirect:/nydelopgave";
        }
        return "redirect:/delprojekt";

    }


    @PostMapping("/nytdelprojekt")
    public String createSubProject(@RequestParam("subProjectName") String subProjectName,
                                @RequestParam("start") String start,
                                @RequestParam("deadline") String deadline,
                                @RequestParam("description") String description,
                                HttpSession session) {
        if (start.length() != 10 || deadline.length() != 10) {
            return "redirect:/nytdelprojekt";
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            SubProject newSubProject = new SubProject();
            LocalDate startDate = LocalDate.parse(start, format);
            LocalDate deadlineDate = LocalDate.parse(deadline, format);
            newSubProject.setProjectId((int) session.getAttribute("projectId"));
            newSubProject.setSubProjectName(subProjectName);
            newSubProject.setStart(startDate);
            newSubProject.setDeadline(deadlineDate);
            newSubProject.setSubProjectDescription(description);
            subProjectRepo.dbAddSubProject(newSubProject);
        } catch (DateTimeParseException e){
            e.printStackTrace();
            return "redirect:/nytdelprojekt";
        }
        return "redirect:/projekt";
    }

    @PostMapping("/redigerprojekt")
    public String editProject(@RequestParam("projectId") int projectId, Model model) {
        model.addAttribute("project", projectRepo.getProject(projectId));
        return "redigerprojekt";
    }



    @PostMapping("/bekræftslet")
    public String confirmProjectDeletion(@RequestParam("projectId") int projectId, Model model){
        model.addAttribute("projectId", projectId);
        return "bekræftsletning";
    }

    @PostMapping("/bekræftsletdelprojekt")
    public String confirmSubProjectDeletion(@RequestParam("subProjectId") int subProjectId, Model model){
        model.addAttribute("subProjectId", subProjectId);
        return "bekræftsletningdelprojekt";
    }



    @PostMapping("/forladprojekt")
    public String leaveproject(@RequestParam("projectId") int projectId,
                               HttpSession session) {
        projectRepo.removeUser((int) session.getAttribute("userId"), projectId);
        return "redirect:/projekter";
    }

    @PostMapping("/redigerdelprojekt")
    public String editSubProject(@RequestParam("subProjectId") int subProjectId, Model model) {
        model.addAttribute("subproject", subProjectRepo.getSubProject(subProjectId));
        return "redigerdelprojekt";
    }

    @PostMapping("/opdaterdelprojekt")
    public String updateSubProject(@RequestParam("subProjectId") int subProjectId,
                                   @ModelAttribute("subproject") SubProject updatedSubProject) {
        SubProject existingSubProject = subProjectRepo.getSubProject(subProjectId);
        existingSubProject.setSubProjectDescription(updatedSubProject.getSubProjectDescription());
        existingSubProject.setStart(updatedSubProject.getStart());
        existingSubProject.setDeadline(updatedSubProject.getDeadline());
        subProjectRepo.subprojectUpdate(existingSubProject);
        return "redirect:/projekt";
    }


}
