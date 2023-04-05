package com.blog.microservices.controller;

import com.blog.microservices.dto.PostDto;
import com.blog.microservices.dto.PostDtoRequest;
import com.blog.microservices.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    @PostMapping("/post")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createPost(@Valid @RequestBody PostDtoRequest postDtoRequest) {
        return new ResponseEntity<>(postService.createPost(postDtoRequest), HttpStatus.CREATED);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPostById(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @PutMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updatePost(@PathVariable("postId") Long postId, @Valid @RequestBody PostDtoRequest postDtoRequest) {
        return ResponseEntity.ok(postService.updatePost(postId, postDtoRequest));
    }

    @DeleteMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deletePost(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(postService.deletePost(postId));
    }
}
