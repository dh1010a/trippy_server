package com.example.server.domain.post.dto;

import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.ticket.dto.TicketResponseDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PostResponseDto {

    @Builder
    @Data
    public static class GetPostResponseDtoGeneric<T> {
        private PostMemberResponseDto member;
        private T content;
        private PostBasicResponseDto post;
        private Boolean isSuccess;
    }

    @Builder
    @Data
    public static class GetPostResponseDto{
        private PostMemberResponseDto member;
        private TicketResponseDto.TicketBasicResponseDto ticket;
        private PostBasicResponseDto post;
        private Boolean isSuccess;
    }

    @Builder
    @Data
    public static class GetOotdPostResponseDto{
        private PostMemberResponseDto member;
        private OotdReqResDto.OotdBasicResponseDto ootd;
        private PostBasicResponseDto post;
        private Boolean isSuccess;
    }

    @Builder
    @Data
    public static class PostBasicResponseDto{
        private Long id;
        private LocalDateTime createDateTime;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<ImageResponseDto.ImageBasicResponseDto> images;
        private List<String> tags;
        private Integer likeCount;
        private Boolean isLiked;
        private Integer commentCount;
        private Integer viewCount;
        private Integer bookmarkCount;

    }

    @Builder
    @Data
    public static class GetIsLikedList{
        private Long postId;
        private Boolean isLiked;
        private Boolean isSuccess;
    }


    @Builder
    @Data
    public static class DeletePostResultResponseDto{
        private Long id;
        private Boolean isSuccess;
    }
    @Builder
    @Data
    public static class PostMemberResponseDto{
        private String memberId;
        private String nickName;
        private String profileUrl;
        private String blogName;
    }


}