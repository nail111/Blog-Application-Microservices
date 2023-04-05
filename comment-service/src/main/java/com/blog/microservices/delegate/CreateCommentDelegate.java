package com.blog.microservices.delegate;

import com.blog.microservices.dto.CommentDtoRequest;
import com.blog.microservices.model.Comment;
import com.blog.microservices.repository.CommentRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(value = "createCommentDelegate")
public class CreateCommentDelegate implements JavaDelegate {

    private final CommentRepository commentRepository;
    private final static Logger LOGGER = LoggerFactory.getLogger(CreateCommentDelegate.class);

    public CreateCommentDelegate(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long postId = (Long) execution.getVariable("postId");
        CommentDtoRequest commentDtoRequest = (CommentDtoRequest) execution.getVariable("commentDtoRequest");

        Comment comment = Comment.builder()
                .name(commentDtoRequest.getName())
                .email(commentDtoRequest.getEmail())
                .body(commentDtoRequest.getBody())
                .build();

        comment.setPostId(postId);

        LOGGER.info("saving comment: {}", comment);
        Comment commentEntity = commentRepository.save(comment);

        execution.setVariable("commentEntity", commentEntity);
    }
}
