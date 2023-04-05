package com.blog.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDto implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String content;
}
