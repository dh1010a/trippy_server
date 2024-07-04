package com.example.server.domain.post.dto;

import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.member.model.Scope;
import com.example.server.domain.post.domain.Ootd;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.ticket.dto.TicketResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.server.domain.image.dto.ImageDtoConverter.convertToImageBasicDto;
import static com.example.server.domain.ticket.dto.TicketDtoConverter.convertToTicketResponseDto;

public class PostDtoConverter {

    public static PostResponseDto.PostBasicResponseDto convertToPostBasicDto(Post post) {

        List<String> tagNames = post.getTag() != null ? post.getTag().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList()) : Collections.emptyList();

        List<ImageResponseDto.ImageBasicResponseDto> convertImage = post.getImages() != null ? post.getImages().stream()
                .filter(image -> image.getImageType() == ImageType.POST)
                .map(image -> convertToImageBasicDto(image))
                .collect(Collectors.toList()) : Collections.emptyList();
        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        int commentCount = post.getComments() != null ? (int) post.getComments().stream().filter(
                comment -> comment.getStatus() != Scope.PRIVATE
        ).count() :0;
        return PostResponseDto.PostBasicResponseDto.builder()
                .id(post.getId())
                .memberId(post.getMember().getMemberId())
                .createDateTime(post.getCreateDate())
                .nickName(post.getMember().getNickName())
                .title(post.getTitle())
                .body(post.getBody())
                .postType(post.getPostType())
                .location(post.getLocation())
                .images(convertImage).tags(tagNames)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }

    public static PostResponseDto.PostDto convertToGetResponseDto(Post post) {
        TicketResponseDto.TicketBasicResponseDto ticket = convertToTicketResponseDto(post.getTicket());
        PostResponseDto.PostBasicResponseDto postDto = convertToPostBasicDto(post);

        return PostResponseDto.PostDto.builder()
                .post(postDto)
                .ticket(ticket)
                .isSuccess(true)
                .build();
    }

    public static PostResponseDto.OotdDto convertToOotdResponseDto(Post post){
        OotdReqResDto.OotdBasicResponseDto ootdDto = convertToOotdBasicResponseDto(post.getOotd());
        PostResponseDto.PostBasicResponseDto postDto = convertToPostBasicDto(post);
        return PostResponseDto.OotdDto.builder()
                .post(postDto)
                .ootd(ootdDto)
                .isSuccess(true)
                .build();
    }

    public static OotdReqResDto.OotdBasicResponseDto convertToOotdBasicResponseDto(Ootd ootd){
        OotdReqResDto.OotdBasicResponseDto ootdBasicResponseDto = OotdReqResDto.OotdBasicResponseDto.builder()
                .id(ootd.getId())
                .area(ootd.getArea())
                .date(ootd.getDate())
                .detailLocation(ootd.getDetailLocation())
                .weatherTemp(ootd.getWeatherTemp())
                .weatherStatus(ootd.getWeatherStatus())
                .build();
        return ootdBasicResponseDto;
    }

    public static PostResponseDto.DeletePostResultResponseDto convertToDeletePostDto(Long id) {
        return PostResponseDto.DeletePostResultResponseDto.builder()
                .id(id)
                .isSuccess(true)
                .build();
    }

    public static  List<PostResponseDto.PostDto> convertToPostListResponseDto(List<Post> posts) {
        List<PostResponseDto.PostDto> postDtos = posts.stream()
                .map(post -> convertToGetResponseDto(post))
                .collect(Collectors.toList());
        return postDtos;
    }

    public static  List<PostResponseDto.OotdDto> convertToOOTDListResponseDto(List<Post> posts) {
        List<PostResponseDto.OotdDto> postDtos = posts.stream()
                .map(post -> convertToOotdResponseDto(post))
                .collect(Collectors.toList());
        return postDtos;
    }
}
