package com.example.server.domain.search.dto;

import lombok.Builder;
import lombok.Data;

public class SearchRequestDto {

    @Builder
    @Data
    public static class SaveSearchRequest{
        private String name;
        private String createdAt;
    }
}
