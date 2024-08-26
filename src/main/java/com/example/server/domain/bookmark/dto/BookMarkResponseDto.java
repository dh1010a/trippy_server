package com.example.server.domain.bookmark.dto;

import lombok.Builder;
import lombok.Data;

public class BookMarkResponseDto {

    @Builder
    @Data
    public static class BookMarkBasicResponse{
        Long bookMarkId;
        Long memberIdx;
        Long postId;
    }

    @Data
    @Builder
    public static class BookMarkCountResponse{
        Long totalCount;
        Long postCount;
        Long ootdCount;
    }
}
