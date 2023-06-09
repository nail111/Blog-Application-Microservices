package com.blog.microservices.service;

import com.blog.microservices.dto.CommentDtoRequest;
import com.blog.microservices.dto.CommentDtoResponse;
import com.blog.microservices.dto.PostDtoResponse;
import com.blog.microservices.exception.CommentException;
import com.blog.microservices.exception.PostException;
import com.blog.microservices.model.Comment;
import com.blog.microservices.repository.CommentRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final FeignClientService feignClientService;

    private final RestTemplate restTemplate;

    private final WebClient webClient;

    private Comment mapToComment(CommentDtoRequest commentDtoRequest) {
        Comment comment = new Comment();
        comment.setName(commentDtoRequest.getName());
        comment.setEmail(commentDtoRequest.getEmail());
        comment.setBody(commentDtoRequest.getBody());
        return comment;
    }

    private CommentDtoResponse mapToCommentDtoResponse(Comment comment) {
        CommentDtoResponse commentDtoResponse = new CommentDtoResponse();
        commentDtoResponse.setId(comment.getId());
        commentDtoResponse.setName(comment.getName());
        commentDtoResponse.setBody(comment.getBody());
        commentDtoResponse.setEmail(comment.getEmail());

        return commentDtoResponse;
    }

    public PostDtoResponse getPost(Long postId) {
        log.info("getting post with id: {}", postId);
        PostDtoResponse postDtoResponse = null;

        try {
            postDtoResponse = feignClientService.getPostById(postId);
            log.info("post with id: {} is found: {}", postId, postDtoResponse);
        } catch (FeignException feignException) {
            throw new PostException("Post with id: " + postId + " is not found");
        }
        return postDtoResponse;
    }

    public Comment getComment(Long commentId) {
        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() -> new CommentException("Comment with id: " + commentId + " is not found"));
        return comment;
    }

    private static void checkIfCommentBelongsToThePost(Long postId, Long commentId, PostDtoResponse postDtoResponse, Comment comment) {
        if (postDtoResponse.getId() != comment.getPostId()) {
            throw new CommentException("Comment with id: " + commentId + " doesn't belong to post with id: " + postId);
        }
    }

    public CommentDtoResponse createComment(Long postId, CommentDtoRequest commentDtoRequest) {
        getPost(postId);

        Comment comment = mapToComment(commentDtoRequest);
        comment.setPostId(postId);

        log.info("saving comment: {}", comment);
        commentRepository.save(comment);
        log.info("comment: {} is saved", comment);
        return mapToCommentDtoResponse(comment);
    }

    public CommentDtoResponse getCommentById(Long postId, Long commentId) {
        PostDtoResponse postDtoResponse = getPost(postId);

        Comment comment = getComment(commentId);

        checkIfCommentBelongsToThePost(postId, commentId, postDtoResponse, comment);

        return mapToCommentDtoResponse(comment);
    }

    public CommentDtoResponse updateComment(Long postId, Long commentId, CommentDtoRequest commentDtoRequest) {
        ResponseEntity<PostDtoResponse> responseEntity = restTemplate
                .getForEntity("http://localhost:8765/api/v1/posts/post/" + postId, PostDtoResponse.class);

        PostDtoResponse postDtoResponse = responseEntity.getBody();

        Comment comment = getComment(commentId);

        checkIfCommentBelongsToThePost(postId, commentId, postDtoResponse, comment);

        comment.setName(commentDtoRequest.getName());
        comment.setEmail(commentDtoRequest.getEmail());
        comment.setBody(commentDtoRequest.getBody());

        log.info("saving comment with id: {}", commentId);
        commentRepository.save(comment);
        log.info("comment with id: {} is saved", commentId);

        return mapToCommentDtoResponse(comment);
    }

    @CircuitBreaker(name = "comment", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "comment", fallbackMethod = "fallbackMethod")
    @Retry(name = "comment", fallbackMethod = "fallbackMethod")
    public CompletableFuture<String> deleteComment(Long postId, Long commentId) {

        PostDtoResponse postDtoResponse = webClient.get()
                .uri("http://localhost:8765/api/v1/posts/post/" + postId)
                .retrieve()
                .bodyToMono(PostDtoResponse.class)
                .block();

        Comment comment = getComment(commentId);

        checkIfCommentBelongsToThePost(postId, commentId, postDtoResponse, comment);

        log.info("deleting comment with id: {}", commentId);
        commentRepository.deleteById(commentId);
        log.info("comment with id: {} is deleted", commentId);

        return CompletableFuture.supplyAsync(() -> "Comment " + comment + " is deleted");
    }

    public CompletableFuture<String> fallbackMethod(RuntimeException runtimeException) {
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong! Please try again later");
    }

    @RabbitListener(queues = {"${spring.rabbitmq.queue}"})
    public void consume(PostDtoResponse postDtoResponse) {
        log.info("Post received from queue: {}", postDtoResponse);
    }
}