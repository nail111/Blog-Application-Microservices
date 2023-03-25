package com.blog.microservices.service;

import com.blog.microservices.dto.PostDto;
import com.blog.microservices.model.Post;
import com.blog.microservices.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PostService postService;

    @Test
    public void createPostTest() {
        // Given
        PostDto postDto = PostDto.builder()
                .id(1L)
                .title("Test post")
                .description("Test description")
                .content("Test content")
                .build();

        Post savedPost = Post.builder()
                .id(postDto.getId())
                .title(postDto.getTitle())
                .description(postDto.getDescription())
                .content(postDto.getContent())
                .build();

        // When
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostDto createdPost = postService.createPost(postDto);

        // Then
        verify(postRepository, times(1)).save(any(Post.class));
//        assertEquals(savedPost.getId(), createdPost.getId());
        assertEquals(savedPost.getTitle(), createdPost.getTitle());
        assertEquals(savedPost.getDescription(), createdPost.getDescription());
        assertEquals(savedPost.getContent(), createdPost.getContent());
    }
}