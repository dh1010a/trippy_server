package com.example.server.domain.member.dto;

import com.example.server.domain.member.model.Scope;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class MemberResponseDto {

    @Data
    @Builder
    public static class MemberTaskResultResponseDto {
        private Long idx;
        private String email;
        private String nickName;
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
    public static class MyInfoResponseDto{
        private Long idx;
        private String memberId;
        private String nickName;
        private String email;
        private String profileImageUrl;
        private String blogName;
        private String blogTitleImgUrl;
        private String blogIntroduce;
        private String activeStatus;
        private String role;
        private String socialType;
        private List<String> koreanInterestedTypes;
        private boolean likeAlert;
        private boolean commentAlert;
        private String ticketScope;
        private String ootdScope;
        private String badgeScope;
        private String followScope;
        private String createdAt;
        private int followerCnt;
        private int followingCnt;
//        private List<FollowMemberInfoDto> followers;
//        private List<FollowMemberInfoDto> followings;

    }

    @Data
    @Builder
    public static class MemberInfoResponseDto{
        private String nickName;
        private String email;
        private String profileImageUrl;
        private String blogName;
        private String blogTitleImgUrl;
        private String blogIntroduce;
        private int followerCnt;
        private int followingCnt;
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

    @Data
    @Builder
    public static class FollowMemberInfoDto {
        private Long idx;
        private String memberId;
        private String nickName;
        private String profileImageUrl;
    }

    @Data
    @Builder
    public static class MemberFollowerResponseDto {
        private int followerCnt;
        private List<FollowMemberInfoDto> followers;
    }



    @Data
    @Builder
    public static class MemberFollowingResponseDto {
        private int followingCnt;
        private List<FollowMemberInfoDto> followings;
    }

    @Data
    @Builder
    public static class MemberGetFollowAvailableResponseDto {
        private boolean isAvailable;
        private Scope status;
        private String message;
    }

    @Data
    @Builder
    public static class MemberTaskSuccessResponseDto {
        private boolean isSuccess;
    }

    @Data
    @Builder
    public static class EmailResponseDto {
        private String email;
    }

    @Data
    @Builder
    public static class BookMarkResponseDto {
        private List<Long> bookMarkList;
    }

    @Data
    @Builder
    public static class MemberInterestResponseDto {
        private List<String> koreanInterestedTypes;
        private int cnt;
    }

}
