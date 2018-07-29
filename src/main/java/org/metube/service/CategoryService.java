package org.metube.service;

import org.metube.domain.model.bindingModel.CategoryCreateBindingModel;
import org.metube.domain.entity.Category;

import java.util.List;

public interface CategoryService {

    void createCategory(CategoryCreateBindingModel categoryCreateBindingModel);

    void removeCategoryById(Integer id);

    Category findById(Integer id);

    List<Category> findAll();

    void editCategory(CategoryCreateBindingModel categoryCreateBindingModel, Integer id);
}
