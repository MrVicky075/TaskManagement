package com.company.usermanagement.controller;

import com.company.usermanagement.constraint.AppConstants;
import com.company.usermanagement.dto.ChangePasswordDTO;
import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.service.TaskService;
import com.company.usermanagement.service.UserService;
import com.company.usermanagement.session.UserLoginSession;
import com.company.usermanagement.utility.TaskPermissionHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final UserLoginSession userLoginSession;
    private final TaskService taskService;
    private final TaskPermissionHelper taskPermissionHelper;
    @GetMapping
    public String dashboard(
            @RequestParam(value = "client", required = false, defaultValue = "all") String client,
            @RequestParam(value = "assignedTo", required = false, defaultValue = "all") String assignedTo,
            @RequestParam(value = "issueType", required = false, defaultValue = "all") String issueType,
            @RequestParam(value = "priority", required = false, defaultValue = "all") String priority,
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @RequestParam(value = "fixedOn", required = false, defaultValue = "all") String fixedOn,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,  // NEW
            @RequestParam(value = "dateTo", required = false) String dateTo,  // NE
            Model model) {

        List<String> clientList = taskService.getDistinctClientNames();
        model.addAttribute("clientList", clientList);

        model.addAttribute("assignedUsers", userService.getAllUsers());
        model.addAttribute("fixedOns", AppConstants.getFixedOnList());
        model.addAttribute("prioritiesList", AppConstants.getPriorityList());
        model.addAttribute("issueTypeList", AppConstants.getIssueTypeList());
        model.addAttribute("statusList", AppConstants.getStatusList());
        //model.addAttribute("tasksList", taskService.getAllTasks());

        // filter data
        List<TaskDTO> tasks = taskService.getFilteredTasks(client, assignedTo, issueType, priority,status,fixedOn, dateFrom, dateTo);
        taskPermissionHelper.applyCanEdit(tasks, userLoginSession);
        model.addAttribute("selectedClient", client);
        model.addAttribute("tasksList",tasks);
        model.addAttribute("selectedAssigned", assignedTo);
        model.addAttribute("selectedIssue", issueType);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedFixedOn", fixedOn);
        model.addAttribute("dateFrom", dateFrom);  // NEW
        model.addAttribute("dateTo", dateTo);  //
        return "dashboard";
    }

    @GetMapping("/change-password/{id}")
    public String changePasswordPage(@PathVariable("id") Long id, @RequestParam(value="mode", defaultValue="admin") String mode, Model model){
        String retVal="redirect:/users";
        model.addAttribute("userId",id);
        ChangePasswordDTO changePassword = new ChangePasswordDTO(); changePassword.setMode(mode);
        model.addAttribute("changePassword", changePassword);

        if(mode.equalsIgnoreCase("admin")){
            retVal= "users/change-password-admin";
        }else {
            retVal= "users/change-password";
        }
        return retVal;
    }
    @PostMapping("/change-password/{id}")
    public String changePassword(@PathVariable Long id, @Valid @ModelAttribute("changePassword") ChangePasswordDTO request, BindingResult result){
        System.out.println(result.getAllErrors());
        if(result.hasErrors()){
            return "dashboard/change-password";
        }
        userService.changePassword(id,request);
        return "redirect:/login";
    }
    @GetMapping("/excelUploadPage")
    public String excelUploadPage(Model model){
        return "excel-upload";
    }

}
