package com.example.server.domain.member.dto;

import com.example.server.domain.image.dto.ImageDto;
import com.example.server.global.auth.oauth2.model.SocialType;
import lombok.*;

import java.util.List;

public class MemberRequestDto {

    @Builder
    @Data
    public static class CreateMemberRequestDto {
        private String memberId;
        private String password;
        private String email;
    }

    @Builder
    @Data
    public static class CommonCreateMemberRequestDto {
        private ImageDto profileImage;
        private String nickName;
        private String blogName;
        private String blogIntroduce;
    }

    @Builder
    @Data
    public static class UpdateMemberRequestDto {
        private ImageDto blogImage;
        private ImageDto profileImage;
        private String nickName;
        private String blogName;
        private String blogIntroduce;
        private List<String> koreanInterestedTypes;
        private boolean likeAlert;
        private boolean commentAlert;
        private String ticketScope;
        private String ootdScope;
        private String badgeScope;
        private String followScope;
    }





    @Builder
    @Data
    public static class ChangePasswordRequestDto {
        private String email;
        private String newPassword;
    }

    @ToString
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UpdateInterestedTypesRequestDto {
        private List<String> koreanInterestedTypes;
    }

    @Builder
    @Data
    public static class UnlinkSocialRequestDto {
        private SocialType socialType;
        private String accessToken;
        private String memberId;

    }
}
