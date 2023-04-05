package com.blog.microservices.service;

import com.blog.microservices.dto.PostDto;
import com.blog.microservices.dto.PostDtoRequest;
import com.blog.microservices.dto.PostDtoResponse;
import com.blog.microservices.exception.PostException;
import com.blog.microservices.model.Post;
import com.blog.microservices.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService {

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.routing.key}")
    private String routingKey;

    private final PostRepository postRepository;

    private final RabbitTemplate rabbitTemplate;

    public PostService(PostRepository postRepository, RabbitTemplate rabbitTemplate) {
        this.postRepository = postRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    private Post mapToPost(PostDtoRequest postDtoRequest) {
        Post post = new Post();
        post.setTitle(postDtoRequest.getTitle());
        post.setDescription(postDtoRequest.getDescription());
        post.setContent(postDtoRequest.getContent());
        return post;
    }

    private PostDtoResponse mapToPostDtoResponse(Post post) {
        PostDtoResponse postDtoResponse = new PostDtoResponse();
        postDtoResponse.setId(post.getId());
        postDtoResponse.setTitle(post.getTitle());
        postDtoResponse.setDescription(post.getDescription());
        postDtoResponse.setContent(post.getContent());
        return postDtoResponse;
    }

    public PostDtoResponse createPost(PostDtoRequest postDtoRequest) {
        log.info("creating post {}...", postDtoRequest.getTitle());
        Post post = mapToPost(postDtoRequest);
        log.info("post {} created", post.getTitle());
        log.info("saving post {}...", post.getTitle());
        postRepository.save(post);

        post.setId(postDtoRequest.getId());
        log.info("post {} saved with id {}", post.getTitle(), post.getId());
        return mapToPostDtoResponse(post);
    }

    public List<PostDtoResponse> getAllPosts() {
        List<PostDtoResponse> postDtoResponseList = postRepository
                .findAll()
                .stream()
                .map(post -> mapToPostDtoResponse(post)).collect(Collectors.toList());

        return postDtoResponseList;
    }

    public PostDtoResponse getPostById(Long postId) {
        Post post = getPost(postId);

        log.info("Sending post: {} to the queue...", post);
        rabbitTemplate.convertAndSend(exchange, routingKey, mapToPostDtoResponse(post));
        log.info("Post: {} has been sent to the queue", post);

        return mapToPostDtoResponse(post);
    }

    private Post getPost(Long postId) {
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() -> new PostException("Post with id: " + postId + " is not found"));
        return post;
    }

    public PostDtoResponse updatePost(Long postId, PostDtoRequest postDtoRequest) {
        log.info("getting post with id: {}...", postId);
        Post post = getPost(postId);
        log.info("post with id: {} is found", postId);

        log.info("update post with id: {}", postId);
        post.setTitle(postDtoRequest.getTitle());
        post.setDescription(postDtoRequest.getDescription());
        post.setContent(postDtoRequest.getContent());
        log.info("post with id: {} is updated", postId);

        log.info("saving post with id: {}", postId);
        postRepository.save(post);
        log.info("post with id: {} is saved", postId);

        return mapToPostDtoResponse(post);
    }

    public String deletePost(Long postId) {
        log.info("getting post with id: {}...", postId);
        Post post = getPost(postId);
        log.info("post with id: {} is found", postId);

        log.info("deleting post with id: {}", postId);
        postRepository.deleteById(postId);
        log.info("post with id: {} is deleted", postId);

        return "Post " + post.getTitle() + " is deleted";
    }
}
