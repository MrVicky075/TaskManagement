package com.company.usermanagement.controller;

import com.company.usermanagement.constraint.AppConstants;
import com.company.usermanagement.dto.ChangePasswordDTO;
import com.company.usermanagement.dto.UserRequestDTO;
import com.company.usermanagement.dto.UserResponseDTO;
import com.company.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUser(Model model){
        model.addAttribute("users",userService.getAllUsers());
        return "users/user-table";
    }

    @GetMapping("/add")
    public String addPage(Model model){
        model.addAttribute("user", new UserRequestDTO());
        model.addAttribute("roleList", AppConstants.getUserRoles());
        return "users/user-form";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") UserRequestDTO request, BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("roleList", AppConstants.getUserRoles());
            return "users/user-form";
        }
        userService.createUser(request);
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model){
        model.addAttribute("user",userService.getUserById(id));
        model.addAttribute("roleList", AppConstants.getUserRoles());
        return "users/user-edit";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") UserResponseDTO request,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roleList", AppConstants.getUserRoles());
            return "users/user-edit";
        }
        userService.updateUser(id, request);
        return "redirect:/users?updated";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users?deleted";
    }

    @PostMapping("/status/{id}")
    public String changeStatus(@PathVariable Long id){
        userService.changeActiveStatus(id);
        return "redirect:/users";
    }

}
