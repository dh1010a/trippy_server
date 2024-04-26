package com.example.server.domain.member.dto;

import lombok.Builder;
import lombok.Data;

public class MemberResponseDto {

    @Data
    @Builder
    public static class MemberTaskResultResponseDto {
        private Long idx;
        private String email;
        private String nickName;
        private Boolean isSuccess;
    }

    @Data
    @Builder
    public static class IsNewMemberResponseDto {
        private Long idx;
        private String provider;
        private String memberId;
        private String email;
        private boolean isNewMember;
    }

}
