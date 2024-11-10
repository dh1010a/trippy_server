package com.example.server.domain.notify.domain;

import com.example.server.domain.notify.dto.NotifyDto;
import com.example.server.domain.notify.service.NotifyService;
import com.example.server.domain.notify.service.SseNotifyService;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.currentThread;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyListener {

    private final SseNotifyService sseNotifyService;
    private final NotifyService notifyService;
//    private final FCMNotifyService fcmNotifyService;
//    private final FCMTokenService fcmTokenService;

    @TransactionalEventListener
    @Async
    public CompletableFuture<Void> handleSseNotification(NotifyDto.NotifyPublishRequestDto requestDto) {
        return CompletableFuture.runAsync(() -> {
                    try {
                        sseNotifyService.sendNotify(requestDto.getReceiver(), requestDto);
//                        if (fcmTokenService.getFCMToken(requestDto.getReceiver().getMemberId()) != null) {
//                            sendFCMNotificationAsync(requestDto.getReceiver().getMemberId(), fcmNotifyService.createFCMMessage(requestDto.getReceiver(), requestDto));
//                        }
                    } catch (Exception e) {
                        log.error("Error while sending SSE notification for receiver: {}", requestDto.getReceiver().getMemberId(), e);
                    }
                }).orTimeout(10, TimeUnit.SECONDS) // 타임아웃을 5초로 설정
                .exceptionally(ex -> {
                    if (ex instanceof TimeoutException) {
                        log.error("Async operation timed out for receiver: {}", requestDto.getReceiver().getMemberId());
                    } else {
                        log.error("Unexpected error occurred during async operation for receiver: {}", requestDto.getReceiver().getMemberId(), ex);
                    }
                    return null;
                });
    }

//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @TransactionalEventListener
//    @Async
//    public void hadleFcmNotification(NotifyDto.NotifyPublishRequestDto requestDto) {
//
//    }


//    private void sendFCMNotification(String memberId, Message message) {
//        try {
//            fcmNotifyService.sendFCMNotification(memberId, message);
//        } catch (Exception e) {
//            log.error("Failed to send FCM Notification. MemberId: [{}], Message: [{}]", memberId, message);
//        }
//    }

//    private void sendFCMNotificationAsync(String memberId, Message message) {
//        try {
//            Future<Void> future = CompletableFuture.runAsync(() -> fcmNotifyService.sendFCMNotification(memberId, message));
//            future.get(10, TimeUnit.SECONDS); // 10초 내에 완료되지 않으면 타임아웃 발생
//        } catch (TimeoutException e) {
//            log.error("FCM notification for memberId {} timed out", memberId, e);
//        } catch (Exception e) {
//            log.error("Error while sending FCM notification for memberId {}", memberId, e);
//        }
//    }
}
