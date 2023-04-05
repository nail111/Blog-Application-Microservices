package com.blog.microservices.service;

import com.blog.microservices.dto.PostDto;
import com.blog.microservices.dto.PostDtoRequest;
import com.blog.microservices.dto.PostDtoResponse;
import com.blog.microservices.model.Post;
import com.blog.microservices.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void createPost() {
        // Arrange
        PostDto postDto = PostDto.builder()
                .id(1L)
                .title("Test Post")
                .description("This is a test post")
                .content("Test post content")
                .build();

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post post = invocation.getArgument(0);
                    post.setId(1L);
                    return post;
                });

        // Act
        PostDto result = postService.createPost(postDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(postDto.getTitle(), result.getTitle());
        assertEquals(postDto.getDescription(), result.getDescription());
        assertEquals(postDto.getContent(), result.getContent());

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void getAllPosts() {
        // arrange
        List<Post> postList = new ArrayList<>();
        postList.add(new Post(1L, "Test Post 1", "Test Description 1", "Test Content 1"));
        postList.add(new Post(2L, "Test Post 2", "Test Description 2", "Test Content 2"));
        when(postRepository.findAll()).thenReturn(postList);

        List<PostDto> expectedPostDtoList = new ArrayList<>();
        expectedPostDtoList.add(PostDto.builder().id(1L).title("Test Post 1").description("Test Description 1").content("Test Content 1").build());
        expectedPostDtoList.add(PostDto.builder().id(2L).title("Test Post 2").description("Test Description 2").content("Test Content 2").build());

        // act
        List<PostDtoResponse> actualPostDtoResponseList = postService.getAllPosts();

        // assert
        assertEquals(expectedPostDtoList, actualPostDtoResponseList);
    }

    @Test
    void getPostById() {
        // Arrange
        Long postId = 1L;
        Post post = new Post(postId, "Test Post", "Test Description", "Test Content");
        PostDtoRequest postDtoRequest = new PostDtoRequest(postId, "Test Post", "Test Description", "Test Content");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // Act
        postService.createPost(postDtoRequest);

        // mock RabbitTemplate
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        // set mock RabbitTemplate on postService

        // There is an error with RabbitMQ
        PostDtoResponse result = postService.getPostById(postId);

        // Assert
        assertEquals(post.getId(), result.getId());
        assertEquals(post.getTitle(), result.getTitle());
        assertEquals(post.getDescription(), result.getDescription());
        assertEquals(post.getContent(), result.getContent());
    }
}