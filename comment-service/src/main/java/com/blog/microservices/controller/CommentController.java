package com.blog.microservices.controller;

import com.blog.microservices.dto.CommentDto;
import com.blog.microservices.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments/post")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{postId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createComment(@PathVariable("postId") Long postId, @Valid @RequestBody CommentDto commentDto) {
        return new ResponseEntity<>(commentService.createComment(postId, commentDto), HttpStatus.CREATED);
    }

    @GetMapping("/{postId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getCommentById(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(postId, commentId));
    }

    @PutMapping("/{postId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        return ResponseEntity.ok(commentService.updateComment(postId, commentId, commentDto));
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(commentService.deleteComment(postId, commentId));
    }
}
