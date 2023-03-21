package com.blog.microservices.exception;


public class PostException extends RuntimeException{
    public PostException(String message) {
        super(message);
    }
}
