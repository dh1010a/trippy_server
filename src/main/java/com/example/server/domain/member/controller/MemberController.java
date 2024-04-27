package com.example.server.domain.member.controller;


import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.member.dto.MemberRequestDto.CreateMemberRequestDto;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.member.service.MemberService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/signup")
    public ApiResponse<?> signUp(@RequestBody CreateMemberRequestDto createMemberRequestDto) {
        return ApiResponse.onSuccess(memberService.signUp(createMemberRequestDto));
    }

    @GetMapping("/myInfo")
    public String getMyInfo() {
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return member.getMemberId();
    }

    @GetMapping("/isNewMember")
    public ApiResponse<?> isNewMember() {
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return ApiResponse.onSuccess(memberService.isNewMember(memberId));
    }

    @GetMapping("/oauth/token")
    public String auth() {
        return "forward:/oauth2/authorize/kakao";
    }


}
