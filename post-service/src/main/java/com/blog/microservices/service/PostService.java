package com.blog.microservices.service;

import com.blog.microservices.dto.PostDto;
import com.blog.microservices.exception.PostException;
import com.blog.microservices.model.Post;
import com.blog.microservices.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.routing.key}")
    private String routingKey;

    private Post mapToPost(PostDto postDto) {
        return Post.builder()
                .title(postDto.getTitle())
                .description(postDto.getDescription())
                .content(postDto.getContent())
                .build();
    }

    private PostDto mapToPostDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .content(post.getContent())
                .build();
    }

    public PostDto createPost(PostDto postDto) {
        log.info("creating post {}...", postDto.getTitle());
        Post post = mapToPost(postDto);
        log.info("post {} created", post.getTitle());
        log.info("saving post {}...", post.getTitle());
        postRepository.save(post);
        log.info("post {} saved", post.getTitle());
        return mapToPostDto(post);
    }

    public List<PostDto> getAllPosts() {
        List<PostDto> postDtoList = postRepository
                .findAll()
                .stream()
                .map(post -> mapToPostDto(post)).collect(Collectors.toList());

        return postDtoList;
    }

    public PostDto getPostById(Long postId) {
        Post post = getPost(postId);

        log.info("Sending post: {} to the queue...", post);
        rabbitTemplate.convertAndSend(exchange, routingKey, mapToPostDto(post));
        log.info("Post: {} has been sent to the queue", post);

        return mapToPostDto(post);
    }

    private Post getPost(Long postId) {
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() -> new PostException("Post with id: " + postId + " is not found"));
        return post;
    }

    public PostDto updatePost(Long postId, PostDto postDto) {
        log.info("getting post with id: {}...", postId);
        Post post = getPost(postId);
        log.info("post with id: {} is found", postId);

        log.info("update post with id: {}", postId);
        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());
        post.setContent(postDto.getContent());
        log.info("post with id: {} is updated", postId);

        log.info("saving post with id: {}", postId);
        postRepository.save(post);
        log.info("post with id: {} is saved", postId);

        return mapToPostDto(post);
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
