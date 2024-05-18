package com.example.server.domain.member.dto;

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
}
