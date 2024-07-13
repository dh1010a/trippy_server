package com.example.server.domain.member.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberResponseDto.*;
import com.example.server.domain.member.model.InterestedType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MemberDtoConverter {

    public static MemberTaskResultResponseDto convertToMemberTaskDto(Member member) {
        return MemberTaskResultResponseDto.builder()
                .idx(member.getIdx())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .build();

    }

    public static MyInfoResponseDto convertToMyInfoResponseDto (Member member) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String profileImageAccessUri = member.getProfileImageAccessUri();
        String getBlogTitleImageAccessUri = member.getProfileImageAccessUri();

        return MyInfoResponseDto.builder()
                .idx(member.getIdx())
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .email(member.getEmail())
                .profileImageUrl(profileImageAccessUri)
                .blogName(member.getBlogName())
                .blogIntroduce(member.getBlogIntroduce())
                .blogTitleImgUrl(getBlogTitleImageAccessUri)
                .role(member.getRole().getTitle())
                .activeStatus(member.getActiveState().name())
                .socialType(member.getSocialType().getSocialName())
                .koreanInterestedTypes(member.getInterestedTypes().stream().map(InterestedType::getTitle).toList())
                .followerCnt(member.getFollowerCnt())
                .followingCnt(member.getFollowingCnt())
                .ticketScope(member.getTicketScope().getKey())
                .ootdScope(member.getOotdScope().getKey())
                .badgeScope(member.getBadgeScope().getKey())
                .followScope(member.getFollowScope().getKey())
                .createdAt(member.getCreatedAt().format(formatter))
                .build();
    }

    public static MemberInfoResponseDto convertToMemberInfoResponseDto (Member member) {
        List<Image> images = member.getImages();
        Image profileImage = images.stream().filter(Image::isProfileImage).findAny().orElse(null);
        Image blogTitleImage = images.stream().filter(Image::isBlogTitleImage).findAny().orElse(null);

        return MemberInfoResponseDto.builder()
                .nickName(member.getNickName())
                .profileImageUrl(profileImage != null ? profileImage.getAccessUri() : null)
                .blogName(member.getBlogName())
                .blogIntroduce(member.getBlogIntroduce())
                .blogTitleImgUrl(blogTitleImage != null ? blogTitleImage.getAccessUri() : null)
                .followerCnt(member.getFollowerCnt())
                .followingCnt(member.getFollowingCnt())
                .build();
    }

    public static MemberFollowResponseDto convertToFollowResponseDto(Member member, Member followingMember) {
        return MemberFollowResponseDto.builder()
                .idx(member.getIdx())
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .followingMemberIdx(followingMember.getIdx())
                .followingMemberId(followingMember.getMemberId())
                .followingMemberNickName(followingMember.getNickName())
                .isSuccess(true)
                .build();
    }

    public static FollowMemberInfoDto convertToFollowMemberInfoDto(Member member) {
        return FollowMemberInfoDto.builder()
                .idx(member.getIdx())
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .build();
    }
}
