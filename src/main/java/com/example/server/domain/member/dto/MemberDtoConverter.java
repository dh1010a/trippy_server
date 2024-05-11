package com.example.server.domain.member.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberResponseDto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MemberDtoConverter {

    public static MemberTaskResultResponseDto convertToMemberTaskDto(Member member) {
        return MemberTaskResultResponseDto.builder()
                .idx(member.getIdx())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .build();

    }

    public static MemberInfoResponseDto convertToInfoResponseDto (Member member) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return MemberInfoResponseDto.builder()
                .idx(member.getIdx())
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .activeStatus(member.getActiveState().name())
                .socialType(member.getSocialType().getSocialName())
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
