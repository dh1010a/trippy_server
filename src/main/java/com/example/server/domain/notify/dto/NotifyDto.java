package com.example.server.domain.notify.dto;

import lombok.*;

public class NotifyDto {

    @Data
    @Builder
    public static class Response {
        String id;
        String nickName;
        String content;
        String type;
        String createdAt;
    }
}
