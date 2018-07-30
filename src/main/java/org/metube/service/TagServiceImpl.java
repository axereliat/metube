package org.metube.service;

import org.metube.entity.Tag;
import org.metube.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public void saveTag(Tag tag) {
        this.tagRepository.saveAndFlush(tag);
    }

    @Override
    public void removeTag(Tag tag) {
        this.tagRepository.deleteById(tag.getId());
    }

    @Override
    public List<Tag> findAll() {
        return this.tagRepository.findAll();
    }

    @Override
    public boolean exists(String name) {
        return this.tagRepository.existsByName(name);
    }
}
