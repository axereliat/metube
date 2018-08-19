package org.metube.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.metube.bindingModel.UserProfileEditBindingModel;
import org.metube.bindingModel.UserRegisterBindingModel;
import org.metube.entity.User;
import org.metube.entity.Video;
import org.metube.service.CloudService;
import org.metube.service.EmailService;
import org.metube.service.RoleService;
import org.metube.service.UserService;
import org.metube.util.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private static final int VIDEOS_PER_PAGE = 6;

    private final UserService userService;

    private final RoleService roleService;

    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, RoleService roleService, EmailService emailService) {
        this.userService = userService;
        this.roleService = roleService;
        this.emailService = emailService;
    }

    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable Integer id, Integer page) {
        if (page == null) page = 1;

        User user = this.userService.findById(id);

        List<Video> videos = user.getVideos().stream()
                .skip(VIDEOS_PER_PAGE * (page - 1))
                .limit(VIDEOS_PER_PAGE)
                .sorted((x1, x2) -> x2.getAddedOn().compareTo(x1.getAddedOn()))
                .collect(Collectors.toList());

        int pages = user.getVideos().size() / VIDEOS_PER_PAGE;

        model.addAttribute("title", "Profile of " + user.getUsername());
        model.addAttribute("currentUser", user);
        model.addAttribute("videos", videos);
        model.addAttribute("pages", pages);
        model.addAttribute("currentPage", page);
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
    public String register(UserRegisterBindingModel userRegisterBindingModel, RedirectAttributes redirAttrs, MultipartFile avatar, @RequestParam(name = "g-recaptcha-response") String gRecaptchaResponse, HttpServletRequest httpServletRequest) {
        return this.userService.registerUser(userRegisterBindingModel, avatar, redirAttrs, gRecaptchaResponse, httpServletRequest);
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

    @GetMapping("/forgotPassword")
    public String forgotPassword(Model model) {
        model.addAttribute("view", "user/forgot-password");

        return "base-layout";
    }

    @PostMapping("/forgotPassword")
    public String forgotPasswordProcess(@RequestParam String email, @RequestParam String username, RedirectAttributes redirectAttributes) {
        RandomString randomString = new RandomString(7);
        String newPassword = randomString.nextString();

        this.emailService.sendSimpleMessage(email, "Your new password for MeTube", "Hello, " + username + "! Your new password is: " + newPassword);

        User user = this.userService.findByUsernameAndEmail(username, email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Wrong username or email!");

            return "redirect:/forgotPassword";
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        user.setPassword(passwordEncoder.encode(newPassword));
        this.userService.save(user);
        redirectAttributes.addFlashAttribute("success", "Check your email and log in with your new password");

        return "redirect:/login";
    }
}
