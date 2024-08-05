package com.example.server.domain.notify.dto;

import lombok.Builder;
import lombok.Data;

public class FCMResponseDto {

    @Data
    @Builder
    public static class FCMTaskResponseDto {
        boolean success;
    }
}
