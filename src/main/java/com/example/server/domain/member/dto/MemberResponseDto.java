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

    @Data
    @Builder
    public static class MemberInfoResponseDto{
        private Long idx;
        private String memberId;
        private String name;
        private String nickName;
        private String email;
        private String phone;
        private String profileImageUrl;
        private String birthDate;
        private String gender;
        private String blogName;
        private String activeStatus;
        private String socialType;

    }

    @Data
    @Builder
    public static class IsDuplicatedDto {
        private String message;
        private boolean isDuplicated;
    }

    @Data
    @Builder
    public static class MemberFollowResponseDto {
        private Long idx;
        private String memberId;
        private String nickName;
        private Long followingMemberIdx;
        private String followingMemberId;
        private String followingMemberNickName;
        private boolean isSuccess;

    }

}
