package com.blog.microservices.delegate;

import com.blog.microservices.dto.PostDtoResponse;
import com.blog.microservices.service.CommentService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(value = "getPostByIdDelegate")
public class GetPostByIdDelegate implements JavaDelegate {

    private final static Logger LOGGER = LoggerFactory.getLogger(CreateCommentDelegate.class);
    private final CommentService commentService;

    public GetPostByIdDelegate(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long postId = (Long) execution.getVariable("postId");

        PostDtoResponse post = commentService.getPost(postId);

        execution.setVariable("post", post);
    }
}
