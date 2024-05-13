package com.example.server.domain.mail.dto;

import lombok.Builder;
import lombok.Data;

public class MailDto {

    @Builder
    @Data
    public static class CheckMailRequestDto {
        private String email;
        private String authNumber;
    }
}
