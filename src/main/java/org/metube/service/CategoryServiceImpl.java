package org.metube.service;

import org.metube.domain.model.bindingModel.CategoryCreateBindingModel;
import org.metube.domain.entity.Category;
import org.metube.exception.ResourceNotFoundException;
import org.metube.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void createCategory(CategoryCreateBindingModel categoryCreateBindingModel) {
        ModelMapper modelMapper = new ModelMapper();
        Category category = modelMapper.map(categoryCreateBindingModel, Category.class);

        this.categoryRepository.saveAndFlush(category);
    }

    @Override
    public void editCategory(CategoryCreateBindingModel categoryCreateBindingModel, Integer id) {
        Category category = this.findById(id);
        category.setName(categoryCreateBindingModel.getName());

        this.categoryRepository.save(category);
    }

    @Override
    public void removeCategoryById(Integer id) {
        this.categoryRepository.deleteById(id);
    }

    @Override
    public Category findById(Integer id) {
        Category category = this.categoryRepository.findById(id).orElse(null);
        if (category == null) throw new ResourceNotFoundException();

        return category;
    }

    @Override
    public List<Category> findAll() {
        return this.categoryRepository.findAll();
    }
}