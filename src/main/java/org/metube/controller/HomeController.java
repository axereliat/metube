package org.metube.controller;

import org.metube.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CategoryService categoryService;

    @Autowired
    public HomeController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        //UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        //        .getAuthentication().getPrincipal();
        //User userEntity = this.userRepository.findByUsername(user.getUsername());

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("view", "home/index");
        return "base-layout";
    }
}
