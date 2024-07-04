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
    public static class GetPostResponseDto{
        private TicketResponseDto.TicketBasicResponseDto ticket;
        private PostBasicResponseDto post;
        private Boolean isSuccess;
    }

    @Builder
    @Data
    public static class GetOotdPostResponseDto{
        private OotdReqResDto.OotdBasicResponseDto ootd;
        private PostBasicResponseDto post;
        private Boolean isSuccess;
    }

    @Builder
    @Data
    public static class PostBasicResponseDto{
        private Long id;
        private LocalDateTime createDateTime;
        private String nickName;
        private String memberId;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<ImageResponseDto.ImageBasicResponseDto> images;
        private List<String> tags;
        private Integer likeCount;
        private Integer commentCount;
    }


    @Builder
    @Data
    public static class DeletePostResultResponseDto{
        private Long id;
        private Boolean isSuccess;
    }


}
