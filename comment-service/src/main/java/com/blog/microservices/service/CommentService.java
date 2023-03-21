package com.blog.microservices.service;

import com.blog.microservices.dto.CommentDto;
import com.blog.microservices.dto.PostDto;
import com.blog.microservices.exception.CommentException;
import com.blog.microservices.exception.PostException;
import com.blog.microservices.model.Comment;
import com.blog.microservices.repository.CommentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final FeignClientService feignClientService;

    private Comment mapToComment(CommentDto commentDto) {
        return Comment.builder()
                .name(commentDto.getName())
                .email(commentDto.getEmail())
                .body(commentDto.getBody())
                .build();
    }

    private CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .name(comment.getName())
                .email(comment.getEmail())
                .body(comment.getBody())
                .build();
    }

    private PostDto getPost(Long postId) {
        log.info("getting post with id: {}", postId);
        PostDto postDto = null;

        try {
            postDto = feignClientService.getPostById(postId);
            log.info("post with id: {} is found: {}", postId, postDto);
        } catch (FeignException feignException) {
            throw new PostException("Post with id: " + postId + " is not found");
        }
        return postDto;
    }

    public CommentDto createComment(Long postId, CommentDto commentDto) {
        getPost(postId);

        Comment comment = mapToComment(commentDto);
        comment.setPostId(postId);

        log.info("saving comment: {}", comment);
        commentRepository.save(comment);
        log.info("comment: {} is saved", comment);
        return mapToCommentDto(comment);
    }

    public CommentDto getCommentById(Long postId, Long commentId) {
        PostDto postDto = getPost(postId);

        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() -> new CommentException("Comment with id: " + commentId + " is not found"));

        if (postDto.getId() != comment.getPostId()) {
            throw new CommentException("Comment with id: " + commentId + " doesn't belong to post with id: " + postId);
        }

        return mapToCommentDto(comment);
    }
}
