package org.metube.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.metube.bindingModel.UserProfileEditBindingModel;
import org.metube.bindingModel.UserRegisterBindingModel;
import org.metube.entity.Role;
import org.metube.entity.User;
import org.metube.enumeration.Gender;
import org.metube.service.RoleService;
import org.metube.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.Collection;

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

    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable Integer id) {
        User user = this.userService.findById(id);
        if (user == null) {
            return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
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
            Path path = Paths.get(property + "/src/main/resources/static/avatars/" + fileName);
            Files.write(path, bytes);
            user.setAvatar(fileName);

            this.userService.registerUser(user);
        } catch (IOException e) {
            e.printStackTrace();
        }

        redirAttrs.addFlashAttribute("success", "You have successfully registered");

        return "redirect:/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/edit")
    public String edit(Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userService.findByUsername(user.getUsername());

        model.addAttribute("user", userEntity);
        model.addAttribute("view", "user/edit");

        return "base-layout";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/edit")
    public String editProcess(UserProfileEditBindingModel userProfileEditBindingModel, RedirectAttributes redirectAttributes, MultipartFile avatar) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userService.findByUsername(user.getUsername());

        if (!userProfileEditBindingModel.getOldPassword().equals("")) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            //String oldPasswordEncrypted = bCryptPasswordEncoder.matches(userProfileEditBindingModel.getOldPassword());

            if (!bCryptPasswordEncoder.matches(userProfileEditBindingModel.getOldPassword(), userEntity.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Incorrect old password!");
                return "redirect:/edit";
            } else {
                userEntity.setPassword(bCryptPasswordEncoder.encode(userProfileEditBindingModel.getNewPassword()));
            }
        }

        if (!avatar.getOriginalFilename().equals("")) {
            try {
                // Get the file and save it somewhere
                byte[] bytes = avatar.getBytes();
                String property = System.getProperty("user.dir");
                String fileName = "avatar_" + userEntity.getId() + this.getFileExtension(avatar.getOriginalFilename());
                Path path = Paths.get(property + "/src/main/resources/static/avatars/" + fileName);
                Files.write(path, bytes);
                userEntity.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!userProfileEditBindingModel.getNewUsername().equals("")) {
            if (!userProfileEditBindingModel.getNewUsername().equals(userEntity.getUsername())) {
                if (this.userService.existsUserWithUsername(userProfileEditBindingModel.getNewUsername())) {
                    redirectAttributes.addFlashAttribute("error", "Username is already taken!");
                    return "redirect:/edit";
                }
            }
            userEntity.setUsername(userProfileEditBindingModel.getNewUsername());
        }
        this.userService.registerUser(userEntity);

        redirectAttributes.addFlashAttribute("success", "Profile successfully edited!");

        return "redirect:/logout";
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
