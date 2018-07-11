package org.metube.service;

import org.metube.entity.Category;
import org.metube.repository.CategoryRepository;
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
    public void createCategory(Category category) {
        this.categoryRepository.saveAndFlush(category);
    }

    @Override
    public void removeCategoryById(Integer id) {
        this.categoryRepository.deleteById(id);
    }

    @Override
    public Category findById(Integer id) {
        return this.categoryRepository.findById(id).orElse(null);
    }

    @Override
    public List<Category> findAll() {
        return this.categoryRepository.findAll();
    }
}