package org.metube.service;

import org.metube.bindingModel.CategoryCreateBindingModel;
import org.metube.entity.Category;

import java.util.List;

public interface CategoryService {

    void createCategory(CategoryCreateBindingModel categoryCreateBindingModel);

    void removeCategoryById(Integer id);

    Category findById(Integer id);

    List<Category> findAll();

    void editCategory(CategoryCreateBindingModel categoryCreateBindingModel, Integer id);
}
