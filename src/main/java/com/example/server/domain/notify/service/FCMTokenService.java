package com.example.server.domain.notify.service;

import com.example.server.domain.notify.dto.FCMResponseDto;
import com.example.server.domain.notify.dto.SaveFCMTokenRequestDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMTokenService {

    public static final String FCM_TOKEN_PREFIX = "fcm_token:";
    public static final long FCM_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 60;

    private final RedisUtil redisUtil;

    public String getFCMToken(String memberId) {
        log.info("Getting FCM Token. MemberId: [{}]", memberId);
        String key = FCM_TOKEN_PREFIX + memberId;
        return redisUtil.get(key, String.class)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.FCM_TOKEN_NOT_FOUND));
    }

    public FCMResponseDto.FCMTaskResponseDto saveFCMToken(SaveFCMTokenRequestDto requestDto, String memberId) {
        String key = FCM_TOKEN_PREFIX + memberId;
        String value = requestDto.fcmToken();
        redisUtil.set(key, value, FCM_TOKEN_EXPIRATION);
        log.info("Saved FCM Token. Member Email: [{}]", memberId);
        return FCMResponseDto.FCMTaskResponseDto.builder()
                .success(true)
                .build();
    }

    public FCMResponseDto.FCMTaskResponseDto removeFCMToken(String memberId) {
        String key = FCM_TOKEN_PREFIX + memberId;
        redisUtil.delete(key);
        log.info("Removed FCM Token. Member Email: [{}]", memberId);
        return FCMResponseDto.FCMTaskResponseDto.builder()
                .success(true)
                .build();
    }
}
