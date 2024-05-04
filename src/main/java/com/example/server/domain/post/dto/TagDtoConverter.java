package com.example.server.domain.post.dto;

import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;

public class TagDtoConverter {

    public static TagResponseDto.TagBasicResponseDto convertToTagBasicResponseDto(Tag tag){

        return TagResponseDto.TagBasicResponseDto.builder().id(tag.getId()).name(tag.getName()).postId(tag.getPost().getId()).build();
    }

    public static Tag convertToTag(TagResponseDto.TagBasicResponseDto tagBasicResponseDto, Post post){

        return Tag.builder().
                id(tagBasicResponseDto.getId())
                .name(tagBasicResponseDto.getName())
                .post(post)
                .build();
    }
}
