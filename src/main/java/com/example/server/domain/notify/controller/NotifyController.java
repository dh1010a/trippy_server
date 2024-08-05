package com.example.server.domain.notify.controller;

import com.example.server.domain.notify.service.NotifyService;
import com.example.server.domain.notify.service.SseNotifyService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Slf4j
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final SseNotifyService sseNotifyService;
    private final NotifyService notifyService;


    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        log.info("subscribe 요청. memberId = {}", memberId);
        return sseNotifyService.subscribe(memberId, lastEventId);
    }


    @GetMapping
    public ApiResponse<?> getNotify() {
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        log.info("notify 조회 요청. memberId = {}", memberId);
        return ApiResponse.onSuccess(notifyService.getAllNotify(memberId));
    }

    @PostMapping("/read")
    public ApiResponse<?> readNotify(@RequestParam Long notifyId) {
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        log.info("notify 읽음 처리 요청. memberId = {}, notifyId = {}", memberId, notifyId);
        notifyService.readNotify(memberId, notifyId);
        return ApiResponse.onSuccess("success");
    }

}
