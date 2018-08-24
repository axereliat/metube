package org.metube.web.controller;

import org.metube.domain.model.bindingModel.UserEditBindingModel;
import org.metube.domain.entity.Role;
import org.metube.domain.entity.User;
import org.metube.service.RoleService;
import org.metube.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    private final RoleService roleService;

    @Autowired
    public AdminUserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("users", this.userService.findAll());
        model.addAttribute("view", "admin/user/list");

        return "base-layout";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model) {
        User user = this.userService.findById(id);

        model.addAttribute("currentUserToDelete", user);
        model.addAttribute("view", "admin/user/delete");

        return "base-layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id) {
        User user = this.userService.findById(id);

        this.userService.deleteUserById(user.getId());

        return "redirect:/admin/users/";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        User user = this.userService.findById(id);

        model.addAttribute("currentUser", user);
        model.addAttribute("roles", this.roleService.findAll());
        model.addAttribute("view", "admin/user/edit");

        return "base-layout";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(UserEditBindingModel userEditBindingModel, @PathVariable Integer id) {
        return this.userService.adminEditUser(userEditBindingModel, id);
    }
}
