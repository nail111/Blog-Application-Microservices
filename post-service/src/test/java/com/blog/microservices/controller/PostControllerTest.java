package com.blog.microservices.controller;

import com.blog.microservices.dto.PostDto;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static String asJsonString(final Object obj) throws NestedServletException {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new NestedServletException("Failed to convert object to JSON string", e);
        }
    }
}