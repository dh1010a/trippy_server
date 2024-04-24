package com.example.server.domain.member.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberResponseDto.IsNewMemberResponseDto;
import com.example.server.domain.member.dto.MemberResponseDto.MemberTaskResultResponseDto;

public class MemberDtoConverter {

    public static MemberTaskResultResponseDto convertToMemberTaskDto(Member member) {
        return MemberTaskResultResponseDto.builder()
                .idx(member.getIdx())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .isSuccess(true)
                .build();

    }

}
