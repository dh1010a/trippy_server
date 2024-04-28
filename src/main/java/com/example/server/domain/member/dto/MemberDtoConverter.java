package com.example.server.domain.member.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberResponseDto.IsNewMemberResponseDto;
import com.example.server.domain.member.dto.MemberResponseDto.MemberInfoResponseDto;
import com.example.server.domain.member.dto.MemberResponseDto.MemberTaskResultResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MemberDtoConverter {

    public static MemberTaskResultResponseDto convertToMemberTaskDto(Member member) {
        return MemberTaskResultResponseDto.builder()
                .idx(member.getIdx())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .isSuccess(true)
                .build();

    }

    public static MemberInfoResponseDto convertToInfoResponseDto (Member member) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return MemberInfoResponseDto.builder()
                .idx(member.getIdx())
                .memberId(member.getMemberId())
                .name(member.getName())
                .nickName(member.getNickName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .profileImageUrl(member.getProfileImageUrl())
                .birthDate(LocalDate.parse(member.getBirthDate().toString(), formatter).toString())
                .gender(member.getGender().name())
//                blogName(member.getBlogName())
                .activeStatus(member.getActiveState().name())
                .socialType(member.getSocialType().getSocialName())
                .build();
    }
}
