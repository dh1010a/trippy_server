package com.example.server.domain.post.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Scope;
import com.example.server.domain.post.domain.Ootd;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.ticket.dto.TicketResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.server.domain.image.dto.ImageDtoConverter.convertToImageBasicDto;
import static com.example.server.domain.post.dto.TagDtoConverter.convertToTagBasicResponseDto;
import static com.example.server.domain.ticket.dto.TicketDtoConverter.convertToTicketResponseDto;

public class PostDtoConverter {

    public static PostResponseDto.PostBasicResponseDto convertToPostBasicDto(Post post) {

        // 태그
        List<String> tagNames = post.getTag() != null ? post.getTag().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList()) : Collections.emptyList();

        // 이미지
        List<ImageResponseDto.ImageBasicResponseDto> convertImage = post.getImages() != null ? post.getImages().stream()
                .filter(image -> image.getImageType() == ImageType.POST)
                .map(image -> convertToImageBasicDto(image))
                .collect(Collectors.toList()) : Collections.emptyList();

        // 좋아요
        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;

        // 댓글
        int commentCount = post.getComments() != null ? (int) post.getComments().stream().filter(
                comment -> comment.getStatus() != Scope.PRIVATE
        ).count() :0;
        return PostResponseDto.PostBasicResponseDto.builder()
                .id(post.getId())
                .createDateTime(post.getCreateDate())
                .title(post.getTitle())
                .body(post.getBody())
                .postType(post.getPostType())
                .location(post.getLocation())
                .images(convertImage).tags(tagNames)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }

    public static PostResponseDto.GetPostResponseDto convertToGetResponseDto(Post post) {
        TicketResponseDto.TicketBasicResponseDto ticket = convertToTicketResponseDto(post.getTicket());
        PostResponseDto.PostBasicResponseDto postDto = convertToPostBasicDto(post);
        PostResponseDto.PostMemberResponseDto memberDto = convertToPostMemberResponseDto(post.getMember());

        return PostResponseDto.GetPostResponseDto.builder()
                .post(postDto)
                .ticket(ticket)
                .member(memberDto)
                .isSuccess(true)
                .build();
    }

    public static PostResponseDto.GetOotdPostResponseDto convertToOotdResponseDto(Post post){
        OotdReqResDto.OotdBasicResponseDto ootdDto = convertToOotdBasicResponseDto(post.getOotd());
        PostResponseDto.PostBasicResponseDto postDto = convertToPostBasicDto(post);
        PostResponseDto.PostMemberResponseDto memberDto = convertToPostMemberResponseDto(post.getMember());
        return PostResponseDto.GetOotdPostResponseDto.builder()
                .post(postDto)
                .ootd(ootdDto)
                .member(memberDto)
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

    public static  List<PostResponseDto.GetPostResponseDto> convertToPostListResponseDto(List<Post> posts) {
        List<PostResponseDto.GetPostResponseDto> postDtos = posts.stream()
                .map(post -> convertToGetResponseDto(post))
                .collect(Collectors.toList());
        return postDtos;
    }

    public static  List<PostResponseDto.GetOotdPostResponseDto> convertToOOTDListResponseDto(List<Post> posts) {
        List<PostResponseDto.GetOotdPostResponseDto> postDtos = posts.stream()
                .map(post -> convertToOotdResponseDto(post))
                .collect(Collectors.toList());
        return postDtos;
    }

    public static PostResponseDto.PostMemberResponseDto convertToPostMemberResponseDto(Member member){
        Image profileImage = member.getImages().stream().filter(Image::isProfileImage).findAny().orElse(null);
        PostResponseDto.PostMemberResponseDto memberDto = PostResponseDto.PostMemberResponseDto.builder()
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .blogName(member.getBlogName())
                .profileUrl(profileImage != null ? profileImage.getAccessUri() : null)
                .build();
        return memberDto;
    }

}