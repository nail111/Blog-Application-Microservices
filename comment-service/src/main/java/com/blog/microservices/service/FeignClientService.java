package com.blog.microservices.service;

import com.blog.microservices.dto.PostDtoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service")
public interface FeignClientService {

    @GetMapping("/api/v1/posts/post/{postId}")
    PostDtoResponse getPostById(@PathVariable("postId") Long postId);
}
