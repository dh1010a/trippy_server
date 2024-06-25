package com.example.server.domain.post.dto;

import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.post.domain.Post;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.server.domain.image.dto.ImageDtoConverter.convertToImageBasicDto;
import static com.example.server.domain.post.dto.TagDtoConverter.convertToTagBasicResponseDto;

public class PostDtoConverter {

    public static PostResponseDto.PostBasicResponseDto convertToPostBasicDto(Post post) {
        List<TagResponseDto.TagBasicResponseDto> convertTag = post.getTag().stream()
                .map(tag -> convertToTagBasicResponseDto(tag))
                .collect(Collectors.toList());

        List<ImageResponseDto.ImageBasicResponseDto> convertImage = post.getImages().stream()
                .map(image -> convertToImageBasicDto(image)).collect(Collectors.toList());

        return PostResponseDto.PostBasicResponseDto.builder()
                .id(post.getId())
                .email(post.getMember().getEmail())
                .title(post.getTitle())
                .body(post.getBody())
                .postType(post.getPostType())
                .location(post.getLocation())
                .images(convertImage).tags(convertTag)
                .isSuccess(true)
                .build();
    }

    public static PostResponseDto.GetPostResponseDto convertToGetResponseDto(Post post) {
        List<TagResponseDto.TagBasicResponseDto> convertTag = post.getTag().stream()
                .map(tag -> convertToTagBasicResponseDto(tag))
                .collect(Collectors.toList());

        List<ImageResponseDto.ImageBasicResponseDto> convertImage = post.getImages().stream()
                .map(image -> convertToImageBasicDto(image)).collect(Collectors.toList());
        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        return PostResponseDto.GetPostResponseDto.builder()
                .id(post.getId())
                .email(post.getMember().getEmail())
                .title(post.getTitle())
                .body(post.getBody())
                .postType(post.getPostType())
                .location(post.getLocation())
                .images(convertImage).tags(convertTag)
                .likeCount(likeCount)
                .isSuccess(true)
                .build();
    }

    public static PostResponseDto.DeletePostResultResponseDto convertToDeletePostDto(Long id) {
        return PostResponseDto.DeletePostResultResponseDto.builder()
                .id(id)
                .isSuccess(true)
                .build();
    }

    public static  List<PostResponseDto.GetPostResponseDto> convertToPostListResponseDto(List<Post> posts) {
        List<PostResponseDto.GetPostResponseDto> postDtos = posts.stream()
                .map(post -> convertToGetResponseDto(post))
                .collect(Collectors.toList());
        return postDtos;
    }
}
