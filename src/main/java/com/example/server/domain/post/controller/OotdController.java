package com.example.server.domain.post.controller;

import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.service.OotdService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ootd")
public class OotdController {

    private final OotdService ootdService;

    @PostMapping("")
    public ApiResponse<?> uploadOotdPost(@RequestBody PostRequestDto.UploadOOTDPostRequestDto requestDto) {
        String memberId = getLoginMemberId();
        requestDto.getPostRequest().setMemberId(memberId);
        log.info("OOTD 게시물 업로드 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(ootdService.uploadOotdPost(requestDto));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
