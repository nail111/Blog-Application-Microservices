package com.blog.microservices.controller;

import com.blog.microservices.dto.CommentDto;
import com.blog.microservices.dto.CommentDtoRequest;
import com.blog.microservices.dto.CommentDtoResponse;
import com.blog.microservices.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments/post")
public class CommentController {
    private final CommentService commentService;
    private final RuntimeService runtimeService;

    @PostMapping("/{postId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createComment(@PathVariable("postId") Long postId, @Valid @RequestBody CommentDtoRequest commentDtoRequest) {
        ProcessInstanceWithVariables processInstanceWithVariables = runtimeService
                .createProcessInstanceByKey("commentById")
                .setVariable("postId", postId)
                .setVariable("commentDtoRequest", commentDtoRequest)
                .executeWithVariablesInReturn();

        VariableMap variableMap = processInstanceWithVariables.getVariables();
        CommentDtoResponse response = variableMap.getValue("response", CommentDtoResponse.class);
//        return new ResponseEntity<>(commentService.createComment(postId, commentDto), HttpStatus.CREATED);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
