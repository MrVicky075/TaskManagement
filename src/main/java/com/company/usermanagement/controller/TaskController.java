package com.company.usermanagement.controller;

import com.company.usermanagement.constraint.AppConstants;
import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.service.TaskService;
import com.company.usermanagement.service.UserService;
import com.company.usermanagement.session.UserLoginSession;
import com.company.usermanagement.utility.TaskPermissionHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final UserService userService;
    private final TaskService taskService;
    private final UserLoginSession userLoginSession;
    private final TaskPermissionHelper taskPermissionHelper;

    @GetMapping
    public String manageTask(Model model){
        boolean allowDelete = false;
        if (!userLoginSession.getRole().name().equalsIgnoreCase("developer")){
            allowDelete=true;
        }
        List<TaskDTO> tasks = taskService.getAllTasks();
        taskPermissionHelper.applyCanEdit(tasks, userLoginSession);
        model.addAttribute("allowDelete",allowDelete);
        model.addAttribute("tasksList", tasks);
        return "task/task-table";
    }

    @GetMapping("/taskForm")
    public String addTask(Model model) {
        model.addAttribute("UserLoginSession", userLoginSession);
        model.addAttribute("task", new TaskDTO());
        model.addAttribute("assignedUsersList", userService.getAllUsers());
        model.addAttribute("fixedOns", AppConstants.getFixedOnList());
        model.addAttribute("prioritiesList", AppConstants.getPriorityList());
        model.addAttribute("paidList", AppConstants.getPaidList());
        model.addAttribute("issueTypeList", AppConstants.getIssueTypeList());
        model.addAttribute("statusList", AppConstants.getStatusList());
        return "task/add-task";
    }

    @PostMapping("/saveTask")
    public String saveTask(TaskDTO taskDTO) {
        if (taskDTO.getTaskId() != null) {
            TaskDTO existing = taskService.getTaskById(taskDTO.getTaskId());
            if (!taskPermissionHelper.canEdit(userLoginSession, existing)) {
                return "error/403";
            }
            taskService.updateTask(taskDTO.getTaskId(), taskDTO);
        } else {
            taskService.saveTask(taskDTO);
        }
        return "redirect:/tasks";
    }

    @GetMapping("/editTask/{id}")
    public String editTask(@PathVariable Long id, Model model) {
        TaskDTO editDTO = taskService.getTaskById(id);
        if (!taskPermissionHelper.canEdit(userLoginSession, editDTO)) {
            return "error/403";
        }

        model.addAttribute("task", editDTO);
        model.addAttribute("assignedUsersList", userService.getAllUsers());
        model.addAttribute("fixedOns", AppConstants.getFixedOnList());
        model.addAttribute("prioritiesList", AppConstants.getPriorityList());
        model.addAttribute("paidList", AppConstants.getPaidList());
        model.addAttribute("issueTypeList", AppConstants.getIssueTypeList());
        model.addAttribute("statusList", AppConstants.getStatusList());
        return "task/add-task";
    }

    @DeleteMapping("/deleteTask/{id}")
    public String deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return "redirect:/dashboard";
    }

    @GetMapping("/deleted")
    public String deletedTasks(Model model) {
        List<TaskDTO> tasks = taskService.getDeletedTasks();
        model.addAttribute("currentPage", "deletedTasks");
        model.addAttribute("tasksList", tasks);
        return "task/deleted-tasks";
    }

    @PostMapping("/rollback/{id}")
    public String rollbackTask(@PathVariable Long id) {
        taskService.restoreTask(id);
        return "redirect:/tasks/deleted";
    }

    @GetMapping("/myTask")
    public String myTask(Model model){
        boolean allowDelete = false;
        if (!userLoginSession.getRole().name().equalsIgnoreCase("developer")){
            allowDelete=true;
        }
        model.addAttribute("allowDelete",allowDelete);
        List<Long> userIds = new ArrayList<>();
        userIds.add(userLoginSession.getUserId());
        List<TaskDTO> tasks = taskService.getMyAllTasks(userIds);
        taskPermissionHelper.applyCanEdit(tasks, userLoginSession);
        model.addAttribute("tasksList", tasks);
        return "task/task-table";
    }
}
