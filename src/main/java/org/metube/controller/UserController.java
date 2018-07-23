package org.metube.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.metube.bindingModel.UserProfileEditBindingModel;
import org.metube.bindingModel.UserRegisterBindingModel;
import org.metube.entity.User;
import org.metube.service.RoleService;
import org.metube.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

@Controller
public class UserController {

    private final UserService userService;

    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable Integer id) {
        User user = this.userService.findById(id);

        model.addAttribute("title", "Profile of " + user.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Register");
        model.addAttribute("view", "user/register");
        return "base-layout";
    }

    @PostMapping("/register")
    public String register(UserRegisterBindingModel userRegisterBindingModel, RedirectAttributes redirAttrs, MultipartFile avatar) {
        return this.userService.registerUser(userRegisterBindingModel, avatar, redirAttrs);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/edit")
    public String edit(Model model, Principal principal) {
        User userEntity = this.userService.findByUsername(principal.getName());

        model.addAttribute("title", "Edit profile");
        model.addAttribute("user", userEntity);
        model.addAttribute("view", "user/edit");

        return "base-layout";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/edit")
    public String editProcess(UserProfileEditBindingModel userProfileEditBindingModel, RedirectAttributes redirectAttributes, MultipartFile avatar) {
        return this.userService.editUser(userProfileEditBindingModel, redirectAttributes, avatar);
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login");
        model.addAttribute("view", "user/login");

        return "base-layout";
    }

    @GetMapping("/logout")
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login?logout";
    }
}
