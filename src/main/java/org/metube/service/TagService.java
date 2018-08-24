package org.metube.service;

import org.metube.domain.entity.Tag;

import java.util.List;

public interface TagService {

    void saveTag(Tag tag);

    void removeTag(Tag tag);

    List<Tag> findAll();

    boolean exists(String name);
}
