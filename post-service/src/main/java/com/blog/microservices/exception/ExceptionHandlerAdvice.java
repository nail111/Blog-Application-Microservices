package com.blog.microservices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(value = PostException.class)
    public ResponseEntity<?> handlePostException(PostException postException) {
        return new ResponseEntity<>(postException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        return new ResponseEntity<>(methodArgumentNotValidException.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(value = CommentException.class)
    public ResponseEntity<?> handleCommentException(CommentException commentException) {
        return new ResponseEntity<>(commentException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
