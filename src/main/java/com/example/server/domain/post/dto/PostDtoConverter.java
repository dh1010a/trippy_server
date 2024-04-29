package com.example.server.domain.post.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.domain.post.domain.Post;

public class PostDtoConverter {

    public static PostResponseDto.UploadPostResultResponseDto convertToMemberTaskDto(Post post) {
        return PostResponseDto.UploadPostResultResponseDto.builder()
                .idx(post.getId())
                .email(post.getMember().getEmail())
                .title(post.getTitle())
                .body(post.getBody())
                .postType(post.getPostType())
                .location(post.getLocation())
                .images(post.getImages()).tags(post.getTag())
                .isSuccess(true)
                .build();

    }
}
