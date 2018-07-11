package org.metube.service;

import org.metube.entity.Comment;

import java.util.List;

public interface CommentService {

    void saveComment(Comment comment);

    void deleteCommentById(Integer id);

    Comment findById(Integer id);
}
