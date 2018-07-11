package org.metube.service;

import org.metube.entity.Comment;
import org.metube.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void saveComment(Comment comment) {
        this.commentRepository.saveAndFlush(comment);
    }

    @Override
    public void deleteCommentById(Integer id) {
        this.commentRepository.deleteById(id);
    }

    @Override
    public Comment findById(Integer id) {
        return this.commentRepository.findById(id).get();
    }
}
