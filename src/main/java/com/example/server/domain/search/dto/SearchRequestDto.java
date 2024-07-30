package com.example.server.domain.search.dto;

import com.example.server.domain.post.model.PostType;
import lombok.Builder;
import lombok.Data;

public class SearchRequestDto {

    @Builder
    @Data
    public static class SaveSearchRequest{
        private String keyword;
        private PostType postType;
        private String searchType;
        private Integer page;
        private Integer size;
    }
}
