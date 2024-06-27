package com.example.server.domain.post.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PostRequestDto {


    @Builder
    @Data
    public static class UploadPostRequestDto{
        private CommonPostRequestDto postRequest;
        private TicketRequestDto.UploadTicketRequestDto ticketRequest;
    }

    @Builder
    @Data
    public static class UploadOOTDPostRequestDto{
        private CommonPostRequestDto postRequest;
        private OotdReqResDto.UploadOOTDRequestDto ootdRequest;
    }

    @Builder
    @Data
    public static class CommonPostRequestDto{
        private String memberId;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<ImageDto> images;
        private List<String> tags;
    }


    public static class UploadTagRequestDto{
        private String name;
        private Post post;
    }

    @Builder
    @Data
    public static class UpdatePostRequestDto{
        private Long id;
        private String memberId;
        private String title;
        private String body;
        private PostType postType;
        private String location;
        private List<ImageDto> images;
        private List<String> tags;
    }
}
