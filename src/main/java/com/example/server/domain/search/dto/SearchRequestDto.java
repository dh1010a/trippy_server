package com.example.server.domain.search.dto;

import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.search.model.SearchType;
import lombok.Builder;
import lombok.Data;

public class SearchRequestDto {

    @Builder
    @Data
    public static class SaveSearchRequest{
        private String keyword;
        private PostType postType;
        private SearchType searchType;
        private OrderType orderType;
        private Integer page;
        private Integer size;
    }
}
