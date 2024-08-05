package com.example.server.domain.notify.domain;

import com.example.server.domain.notify.dto.NotifyDto;
import com.example.server.domain.notify.service.FCMNotifyService;
import com.example.server.domain.notify.service.NotifyService;
import com.example.server.domain.notify.service.SseNotifyService;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static java.lang.Thread.currentThread;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyListener {

    private final SseNotifyService sseNotifyService;
    private final NotifyService notifyService;
    private final FCMNotifyService fcmNotifyService;

    @TransactionalEventListener
    @Async
    public void handleSseNotification(NotifyDto.NotifyPublishRequestDto requestDto) {
        sseNotifyService.sendNotify(requestDto.getReceiver(), requestDto);
    }

    @TransactionalEventListener
//    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    public void hadleFcmNotification(NotifyDto.NotifyPublishRequestDto requestDto) {
        sendFCMNotification(requestDto.getReceiver().getMemberId(), fcmNotifyService.createFCMMessage(requestDto.getReceiver(), requestDto));
    }


    private void sendFCMNotification(String memberId, Message message) {
        fcmNotifyService.sendFCMNotification(memberId, message);
    }

    private void sendFCMNotificationAsync(String memberId, Message message) {
        log.info("Start Sending Asynchronous FCM Notification. " +
                "Current Async Thread Name: [{}]", currentThread().getName());
        fcmNotifyService.sendFCMNotificationAsync(memberId, message);
        log.info("End Asynchronous FCM Notification Sending. " +
                "Current Async Thread Name: [{}]", currentThread().getName());
    }
}
