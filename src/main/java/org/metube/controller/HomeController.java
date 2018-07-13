package org.metube.controller;

import org.metube.entity.User;
import org.metube.exception.ResourceNotFoundException;
import org.metube.service.CategoryService;
import org.metube.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
//@ControllerAdvice(annotations = Controller.class)
public class HomeController {

    private final CategoryService categoryService;

    private final UserService userService;

    @Autowired
    public HomeController(CategoryService categoryService, UserService userServicey) {
        this.categoryService = categoryService;
        this.userService = userServicey;
    }

    //@ModelAttribute
    public void populateModel(Model model) {
        final Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (!principal.getClass().equals(String.class)) {
            UserDetails user = (UserDetails) principal;
            User userEntity = this.userService.findByUsername(user.getUsername());

            model.addAttribute("user", userEntity);
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("view", "home/index");
        return "base-layout";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Model model) {
        return "error/404";
    }
}
