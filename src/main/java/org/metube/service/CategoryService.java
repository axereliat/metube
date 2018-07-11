package org.metube.service;

import org.metube.entity.Category;

import java.util.List;

public interface CategoryService {

    void createCategory(Category category);

    void removeCategoryById(Integer id);

    Category findById(Integer id);

    List<Category> findAll();
}
