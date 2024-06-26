package com.example.server.domain.post.dto;

import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.ticket.dto.TicketResponseDto;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.server.domain.image.dto.ImageDtoConverter.convertToImageBasicDto;
import static com.example.server.domain.post.dto.TagDtoConverter.convertToTagBasicResponseDto;
import static com.example.server.domain.ticket.dto.TicketDtoConverter.convertToTicketResponseDto;

public class PostDtoConverter {

    public static PostResponseDto.PostBasicResponseDto convertToPostBasicDto(Post post) {
//        List<TagResponseDto.TagBasicResponseDto> convertTag = post.getTag().stream()
//                .map(tag -> convertToTagBasicResponseDto(tag))
//                .collect(Collectors.toList());

        List<String> tagNames = post.getTag().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList());

        List<ImageResponseDto.ImageBasicResponseDto> convertImage = post.getImages().stream()
                .filter(image -> image.getImageType() == ImageType.POST)
                .map(image -> convertToImageBasicDto(image)).collect(Collectors.toList());

        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
//        TicketResponseDto.TicketBasicResponseDto ticket = convertToTicketResponseDto(post.getTicket());
        return PostResponseDto.PostBasicResponseDto.builder()
                .id(post.getId())
                .email(post.getMember().getEmail())
                .title(post.getTitle())
                .body(post.getBody())
                .postType(post.getPostType())
                .location(post.getLocation())
                .images(convertImage).tags(tagNames)
                .likeCount(likeCount)
                .build();
    }

    public static PostResponseDto.GetPostResponseDto convertToGetResponseDto(Post post) {
//        List<TagResponseDto.TagBasicResponseDto> convertTag = post.getTag().stream()
//                .map(tag -> convertToTagBasicResponseDto(tag))
//                .collect(Collectors.toList());
//
//        List<ImageResponseDto.ImageBasicResponseDto> convertImage = post.getImages().stream()
//                .map(image -> convertToImageBasicDto(image)).collect(Collectors.toList());
//        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;

        TicketResponseDto.TicketBasicResponseDto ticket = convertToTicketResponseDto(post.getTicket());
        PostResponseDto.PostBasicResponseDto postDto = convertToPostBasicDto(post);

        return PostResponseDto.GetPostResponseDto.builder()
                .post(postDto)
                .ticket(ticket)
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
