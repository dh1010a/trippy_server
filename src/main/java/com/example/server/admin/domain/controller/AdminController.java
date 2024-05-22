package com.example.server.admin.domain.controller;

import com.example.server.domain.image.service.ImageService;
import com.example.server.domain.image.service.OracleImageService;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.member.service.MemberService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final ImageService imageService;
    private final OracleImageService oracleImageService;
    private final MemberRepository memberRepository;
    private final MemberService memberService;


    @DeleteMapping("/image/all")
    public ApiResponse<?> deleteAllImage() throws Exception{
        log.info("이미지 전체 삭제 요청. AdminId = {}", getLoginMemberId());
        return ApiResponse.onSuccess(oracleImageService.deleteAllImage());
    }

    @DeleteMapping("/preAuth/all")
    public ApiResponse<?> deletePreAuth() throws Exception{
        log.info("사전요청 전체 삭제 요청. AdminId = {}", getLoginMemberId());
        return ApiResponse.onSuccess(oracleImageService.deleteAllPreAuth());
    }

    // 개발 단계에서만 사용하는 API
    @DeleteMapping("/member")
    public ApiResponse<?> deleteMember(@RequestParam(value = "memberId") String memberId) {
        log.info("회원 삭제 요청 : AdminId = {}, memberId = {}", getLoginMemberId(),memberId);
        return ApiResponse.onSuccess( memberService.deleteByMemberId(memberId));

    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
