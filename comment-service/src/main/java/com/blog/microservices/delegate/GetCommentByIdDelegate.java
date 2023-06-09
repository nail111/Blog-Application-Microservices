package com.blog.microservices.delegate;

import com.blog.microservices.dto.CommentDtoResponse;
import com.blog.microservices.dto.PostDtoResponse;
import com.blog.microservices.exception.CommentException;
import com.blog.microservices.model.Comment;
import com.blog.microservices.service.CommentService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(value = "getCommentByIdDelegate")
public class GetCommentByIdDelegate implements JavaDelegate {

    private final static Logger LOGGER = LoggerFactory.getLogger(GetCommentByIdDelegate.class);
    private final CommentService commentService;

    public GetCommentByIdDelegate(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long postId = (Long) execution.getVariable("postId");
        Long commentId = (Long) execution.getVariable("commentId");

        PostDtoResponse postDtoResponse = commentService.getPost(postId);
        Comment comment = commentService.getComment(commentId);

        if (postDtoResponse.getId() != comment.getPostId()) {
            throw new CommentException("Comment with id: " + commentId + " doesn't belong to post with id: " + postId);
        }

        CommentDtoResponse commentDtoResponse = new CommentDtoResponse();
        commentDtoResponse.setId(comment.getId());
        commentDtoResponse.setName(comment.getName());
        commentDtoResponse.setBody(comment.getBody());
        commentDtoResponse.setEmail(comment.getEmail());

        execution.setVariable("commentDtoResponse", commentDtoResponse);
    }
}