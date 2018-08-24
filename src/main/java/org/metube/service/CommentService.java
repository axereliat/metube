package org.metube.service;

import org.metube.domain.entity.Comment;

public interface CommentService {

    void saveComment(Comment comment);

    void deleteCommentById(Integer id);

    Comment findById(Integer id);
}
