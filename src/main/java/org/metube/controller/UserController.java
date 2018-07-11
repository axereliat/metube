package org.metube.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.metube.bindingModel.UserRegisterBindingModel;
import org.metube.entity.Role;
import org.metube.entity.User;
import org.metube.enumeration.Gender;
import org.metube.service.RoleService;
import org.metube.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class UserController {

    private final UserService userService;

    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    private String getFileExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');

        return originalFilename.substring(dotIndex);
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("view", "user/register");
        return "base-layout";
    }

    @PostMapping("/register")
    public String register(UserRegisterBindingModel userRegisterBindingModel, RedirectAttributes redirAttrs, MultipartFile avatar) {
        String error = null;

        if (userRegisterBindingModel.getUsername().equals("") || userRegisterBindingModel.getPassword().equals("") || userRegisterBindingModel.getBirthdate().equals("")) {
            error = "Please fill in all fields.";
            redirAttrs.addFlashAttribute("error", error);
            redirAttrs.addFlashAttribute("username", userRegisterBindingModel.getUsername());

            return "redirect:/register";
        }

        if (!userRegisterBindingModel.getPassword().equals(userRegisterBindingModel.getConfirmPassword())) {
            error = "Passwords do not match.";
            redirAttrs.addFlashAttribute("error", error);
            redirAttrs.addFlashAttribute("username", userRegisterBindingModel.getUsername());

            return "redirect:/register";
        }

        if (this.userService.existsUserWithUsername(userRegisterBindingModel.getUsername())) {
            error = "Username " + userRegisterBindingModel.getUsername() + " is already taken.";
            redirAttrs.addFlashAttribute("error", error);
            redirAttrs.addFlashAttribute("username", userRegisterBindingModel.getUsername());

            return "redirect:/register";
        }


        ModelMapper modelMapper = new ModelMapper();
        User user = modelMapper.map(userRegisterBindingModel, User.class);

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        Role role = this.roleService.findByName("USER");
        user.addRole(role);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate parsedDate = LocalDate.parse(userRegisterBindingModel.getBirthdate(), formatter);

        user.setBirthdate(parsedDate);

        user.setGender(Gender.valueOf(userRegisterBindingModel.getGender().toUpperCase()));

        this.userService.registerUser(user);
        try {
            // Get the file and save it somewhere
            byte[] bytes = avatar.getBytes();
            String property = System.getProperty("user.dir");
            String fileName = "avatar_" + user.getId() + getFileExtension(avatar.getOriginalFilename());
            Path path = Paths.get(property + "/src/main/resources/avatars/" + fileName);
            Files.write(path, bytes);
            user.setAvatar(fileName);

            this.userService.registerUser(user);
        } catch (IOException e) {
            e.printStackTrace();
        }

        redirAttrs.addFlashAttribute("success", "You have successfully registered");

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
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
