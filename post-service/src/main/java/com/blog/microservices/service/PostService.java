package com.blog.microservices.service;

import com.blog.microservices.dto.PostDto;
import com.blog.microservices.model.Post;
import com.blog.microservices.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;

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
        log.info("creating a post...");
        Post post = mapToPost(postDto);
        log.info("post created");
        log.info("saving a post...");
        postRepository.save(post);
        log.info("post saved");
        return mapToPostDto(post);
    }
}
