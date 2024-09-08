package com.example.server.domain.mail.dto;

import lombok.Builder;
import lombok.Data;

public class MailResponseDto {

    @Data
    @Builder
    public static class CheckMailResponseDto {
        private boolean isSuccess;
    }

    @Data
    @Builder
    public static class CheckMailSuccessResponseDto {
        private boolean isSuccess;
        private String authToken;
    }
}
