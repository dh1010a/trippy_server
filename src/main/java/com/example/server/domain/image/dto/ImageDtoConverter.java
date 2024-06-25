package com.example.server.domain.image.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.TagResponseDto;

public class ImageDtoConverter {

    public static ImageResponseDto.ImageBasicResponseDto convertToImageBasicDto(Image image) {
//        Long postId = image.getPost() != null ? image.getPost().getId() : 0;
       /// Long memberId = image.getMember() != null ? image.getMember().getIdx() : 0;

        return ImageResponseDto.ImageBasicResponseDto.builder()
                .imgUrl(image.getImgUrl())
                .authenticateId(image.getAuthenticateId())
                .accessUri(image.getAccessUri())
                .build();
    }

    public static Image convertToImage(ImageResponseDto.ImageBasicResponseDto imageBasicResponseDto, Post post, Member member){

        return Image.builder()
                .imgUrl(imageBasicResponseDto.getImgUrl())
                .post(post)
                .member(member)
                .build();
    }

}
