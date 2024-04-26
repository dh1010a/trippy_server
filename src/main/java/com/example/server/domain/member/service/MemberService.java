package com.example.server.domain.member.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberDtoConverter;
import com.example.server.domain.member.dto.MemberRequestDto.CreateMemberRequestDto;
import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.domain.member.dto.MemberResponseDto.IsNewMemberResponseDto;
import com.example.server.domain.member.dto.MemberResponseDto.MemberTaskResultResponseDto;
import com.example.server.domain.member.model.ActiveState;
import com.example.server.domain.member.model.Gender;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.security.model.ProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberTaskResultResponseDto signUp(CreateMemberRequestDto requestDto) {
        if (isExistByEmail(requestDto.getEmail())) {
            throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);
        }
        if (isExistByNickName(requestDto.getNickName())) {
            throw new ErrorHandler(ErrorStatus.MEMBER_NICKNAME_ALREADY_EXIST);
        }
        Member member = Member.builder()
                .memberId(requestDto.getMemberId())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .nickName(requestDto.getNickName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .gender(Gender.fromName(requestDto.getGender()))
                .activeState(ActiveState.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .role(Role.ROLE_MEMBER)
                .build();
        memberRepository.save(member);
        return MemberDtoConverter.convertToMemberTaskDto(member);
    }

    public IsNewMemberResponseDto isNewMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        boolean isNewMember = member.getRole() == Role.ROLE_GUEST;

        return IsNewMemberResponseDto.builder()
                .idx(member.getIdx())
                .provider(member.getProviderType().toString())
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .isNewMember(isNewMember)
                .build();
    }

    public boolean isExistByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean isExistByNickName(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }

    public boolean isExistByMemberId(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }
}
