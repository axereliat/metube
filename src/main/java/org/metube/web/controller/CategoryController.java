package org.metube.web.controller;

import org.metube.domain.model.bindingModel.CategoryCreateBindingModel;
import org.metube.domain.entity.Category;
import org.metube.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("view", "admin/category/list");

        return "base-layout";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("title", "Add category");
        model.addAttribute("view", "admin/category/create");

        return "base-layout";
    }

    @PostMapping("/create")
    public String createProcess(CategoryCreateBindingModel categoryCreateBindingModel) {
        this.categoryService.createCategory(categoryCreateBindingModel);

        return "redirect:/admin/categories/";
    }

    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Integer id) {
        Category category = this.categoryService.findById(id);

        model.addAttribute("title", "Delete category");
        model.addAttribute("category", category);
        model.addAttribute("view", "admin/category/delete");

        return "base-layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id) {
        Category category = this.categoryService.findById(id);

        this.categoryService.removeCategoryById(category.getId());

        return "redirect:/admin/categories/";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Integer id) {
        Category category = this.categoryService.findById(id);

        model.addAttribute("title", "Edit category");
        model.addAttribute("category", category);
        model.addAttribute("view", "admin/category/edit");

        return "base-layout";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(CategoryCreateBindingModel categoryCreateBindingModel, @PathVariable Integer id) {
        this.categoryService.editCategory(categoryCreateBindingModel, id);

        return "redirect:/admin/categories/";
    }
}
