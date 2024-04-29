package com.example.server.domain.blog.dto;

import com.example.server.domain.blog.domain.Blog;
import com.example.server.domain.blog.dto.BlogResponseDto.CreateBlogResponseDto;

public class BlogDtoConverter {

    public static CreateBlogResponseDto convertToBlogResponseDto(Blog blog) {
        return CreateBlogResponseDto.builder()
                .id(blog.getId())
                .name(blog.getName())
                .isSuccess(true)
                .build();
    }
}
