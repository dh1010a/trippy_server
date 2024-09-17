package com.example.server.domain.search.dto;

import lombok.Builder;
import lombok.Data;

public class SearchResponseDto {

    @Builder
    @Data
    public static class SearchMemberDto{
        private String memberId;
        private String nickName;
        private String profileImgUrl;
        private String blogTitleImgUrl;
        private String blogIntroduction;
        private String blogName;
        private int followerCnt;
        private int followingCnt;
    }
}
