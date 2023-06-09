package com.blog.microservices.controller;

import com.blog.microservices.dto.PostDto;
import com.blog.microservices.dto.PostDtoRequest;
import com.blog.microservices.dto.PostDtoResponse;
import com.blog.microservices.model.Post;
import com.blog.microservices.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    @Test
    public void createPostTest() throws Exception {
        // Given
        PostDto postDto = PostDto.builder()
                .id(1L)
                .title("title")
                .description("description")
                .content("content")
                .build();

        // When
        when(postService.createPost(postDto)).thenReturn(postDto);

        // Then
        mockMvc.perform(post("/api/v1/posts/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(postDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(postDto.getTitle())))
                .andExpect(jsonPath("$.content", is(postDto.getContent())))
                .andDo(print());
    }

    @Test
    public void getAllPostsTest() throws Exception {
        // Given
        List<PostDtoResponse> posts = Arrays.asList(
                new PostDtoResponse(1L, "title 1", "description 1", "content 1"),
                new PostDtoResponse(2L, "title 2", "description 2", "content 2")
        );

        // When
        when(postService.getAllPosts()).thenReturn(posts);

        // Then
        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("title 1")))
                .andExpect(jsonPath("$[0].content", is("content 1")))
                .andExpect(jsonPath("$[1].title", is("title 2")))
                .andExpect(jsonPath("$[1].content", is("content 2")))
                .andDo(print());
    }

    @Test
    public void getPostByIdTest() throws Exception {
        // Given
        Long postId = 1L;
        PostDtoResponse postDtoResponse = new PostDtoResponse(1L, "title 1", "description 1", "content 1");

        // When
        when(postService.getPostById(postId)).thenReturn(postDtoResponse);

        // Then
        mockMvc.perform(get("/api/v1/posts/post/" + postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("title 1")))
                .andExpect(jsonPath("$.content", is("content 1")))
                .andDo(print());
    }

    @Test
    public void updatePostTest() throws Exception {
        // Given
        Long postId = 1L;
        PostDtoRequest postDtoRequest = new PostDtoRequest(1L, "title 1", "description 1", "content 1");
        PostDtoResponse postDtoResponse = new PostDtoResponse(1L, "title 1 updated", "description 1 updated", "content 1 updated");

        // When
        when(postService.updatePost(postId, postDtoRequest)).thenReturn(postDtoResponse);

        // Then
        mockMvc.perform(put("/api/v1/posts/post/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(postDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("title 1 updated")))
                .andExpect(jsonPath("$.content", is("content 1 updated")))
                .andExpect(jsonPath("$.description", is("description 1 updated")))
                .andExpect(jsonPath("$.id", is(1)))
                .andDo(print());
    }

    @Test
    public void deletePostTest() throws Exception {
        // Given
        Long postId = 1L;
        Post post = new Post(1L, "title 1", "description 1", "content 1");
        String expectedMessage = "Post title 1 is deleted";

        // When
        when(postService.deletePost(postId)).thenReturn("Post " + post.getTitle() + " is deleted");

        // Then
        mockMvc.perform(delete("/api/v1/posts/post/" + postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage))
                .andDo(print());

        verify(postService, times(1)).deletePost(eq(postId));
    }

    private static String asJsonString(final Object obj) throws NestedServletException {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new NestedServletException("Failed to convert object to JSON string", e);
        }
    }
}