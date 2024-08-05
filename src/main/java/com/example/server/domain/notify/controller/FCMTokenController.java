package com.example.server.domain.notify.controller;

import com.example.server.domain.notify.dto.SaveFCMTokenRequestDto;
import com.example.server.domain.notify.service.FCMTokenService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Slf4j
public class FCMTokenController {

    private final FCMTokenService fcmTokenService;

    @PostMapping("/register")
    public ApiResponse<?> saveFCMToken(
            @RequestBody SaveFCMTokenRequestDto saveFCMTokenRequest) {
        return ApiResponse.onSuccess(fcmTokenService.saveFCMToken(saveFCMTokenRequest, getLoginMemberId()));
    }

    @DeleteMapping("/unregister")
    public ApiResponse<?> removeFCMToken() {
        return ApiResponse.onSuccess(fcmTokenService.removeFCMToken(getLoginMemberId()));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
