//package com.example.server.domain.notify.service;
//
//import com.example.server.domain.member.domain.Member;
//import com.example.server.domain.notify.domain.Notify;
//import com.example.server.domain.notify.dto.NotifyDto;
//import com.example.server.domain.notify.dto.NotifyDtoConverter;
//import com.example.server.domain.notify.dto.NotifyResponseDto;
//import com.example.server.domain.notify.model.NotificationType;
//import com.example.server.domain.notify.repository.NotifyRepository;
//import com.example.server.global.apiPayload.code.status.ErrorStatus;
//import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
//import com.google.api.core.ApiFuture;
//import com.google.firebase.messaging.*;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationContext;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Recover;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class FCMNotifyService {
//
//    private final FCMTokenService fcmTokenService;
//    private final FirebaseMessaging firebaseMessaging;
//    private final NotifyRepository notifyRepository;
//    private final NotifyService notifyService;
//
//    public static final String TARGET_ID_KEY = "targetId";
//    public static final String TARGET_TYPE_KEY = "targetType";
//
//    private final Executor callBackTaskExecutor;
//
//    private final ApplicationContext applicationContext;
//
//    public Message createFCMMessage(Member receiver, NotifyDto.NotifyPublishRequestDto requestDto) {
//        String memberId = receiver.getMemberId();
//        Notify notification = notifyRepository.save(notifyService.createNotify(receiver, requestDto));
//        return Message.builder()
//                .setToken(fcmTokenService.getFCMToken(memberId))
//                .setNotification(createNotification(notification.getTitle(), notification.getContent(), notification.getSenderProfileImgUri()))
//                .putAllData(createData(NotifyDtoConverter.convertToInfoResponseDto(notification)))
//                .build();
//    }
//
//    private Notification createNotification(String title, String body, String imgUri) {
//        return Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .setImage(imgUri)
//                .build();
//    }
//
//    private Map<String, String> createData(NotifyResponseDto.NotifyInfoDto notifyInfoDto) {
//        Map<String, String> data = new HashMap<>();
//        data.put("notifyId", notifyInfoDto.getNotifyId() != null ? String.valueOf(notifyInfoDto.getNotifyId()) : "");
//        data.put("title", notifyInfoDto.getTitle() != null ? notifyInfoDto.getTitle() : "");
//        data.put("content", notifyInfoDto.getContent() != null ? notifyInfoDto.getContent() : "");
//        data.put("senderProfileImgUri", notifyInfoDto.getSenderProfileImgUri() != null ? notifyInfoDto.getSenderProfileImgUri() : "");
//        data.put("senderNickName", notifyInfoDto.getSenderNickName() != null ? notifyInfoDto.getSenderNickName() : "");
//        data.put("senderMemberId", notifyInfoDto.getSenderMemberId() != null ? notifyInfoDto.getSenderMemberId() : "");
//        data.put("postId", notifyInfoDto.getPostId() != null ? String.valueOf(notifyInfoDto.getPostId()) : "");
//        data.put("postTitle", notifyInfoDto.getPostTitle() != null ? notifyInfoDto.getPostTitle() : "");
//        data.put("isRead", String.valueOf(notifyInfoDto.isRead()));
//        data.put("notificationType", notifyInfoDto.getNotificationType() != null ? notifyInfoDto.getNotificationType() : "");
//        data.put("createdAt", notifyInfoDto.getCreatedAt() != null ? notifyInfoDto.getCreatedAt() : "");
//        return data;
//    }
//
//    @Async
//    public void sendFCMNotificationAsync(String memberId, Message message) {
//        ApiFuture<String> apiFuture = firebaseMessaging.sendAsync(message);
//        apiFuture.addListener(() -> {
//            try {
//                String response = apiFuture.get();
//                log.info("FCM Notification Sent Successfully. Message ID: [{}]", response);
//                log.info("Current Call Back Thread Name: [{}]", Thread.currentThread().getName());
//            } catch (InterruptedException | ExecutionException executionException) {
//                if (executionException.getCause() instanceof FirebaseMessagingException firebaseMessagingException) {
//                    MessagingErrorCode errorCode = firebaseMessagingException.getMessagingErrorCode();
//                    if (isRetryMessagingErrorCode(errorCode)) {
//                        applicationContext.getBean(FCMNotifyService.class).sendFCMNotification(memberId, message);
//                        return;
//                    }
//                    handleFCMMessagingException(errorCode, memberId);
//                }
//            }
//        }, callBackTaskExecutor);
//    }
//
//    @Retryable(
//            retryFor = RuntimeException.class,
//            backoff = @Backoff(delay = 1000, multiplier = 2)
//    )
//    public void sendFCMNotification(String memberId, Message message) {
//        try {
//            String response = firebaseMessaging.send(message);
//            log.info("FCM Notification Sent Successfully. Message ID: [{}]", response);
//        } catch (FirebaseMessagingException firebaseMessagingException) {
//            MessagingErrorCode errorCode = firebaseMessagingException.getMessagingErrorCode();
//            if (isRetryMessagingErrorCode(errorCode)) {
//                throw new RuntimeException();
//            }
//            handleFCMMessagingException(errorCode, memberId);
//        }
//    }
//
//    private boolean isRetryMessagingErrorCode(MessagingErrorCode errorCode) {
//        if (errorCode.equals(MessagingErrorCode.INTERNAL) || errorCode.equals(MessagingErrorCode.UNAVAILABLE)) {
//            log.info("Failed To Send FCM Notification. FCM Server Error. Retrying...");
//            return true;
//        }
//        return false;
//    }
//
//    private void handleFCMMessagingException(MessagingErrorCode errorCode, String memberId) {
//        if (errorCode.equals(MessagingErrorCode.UNREGISTERED) || errorCode.equals(MessagingErrorCode.INVALID_ARGUMENT)) {
//            log.info("Failed To Send FCM Notification. Invalid FCM Token. Deleting...");
//            fcmTokenService.removeFCMToken(memberId);
//            throw new ErrorHandler(ErrorStatus.FCM_TOKEN_NOT_FOUND);
//        } else if (errorCode.equals(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR) || errorCode.equals(MessagingErrorCode.SENDER_ID_MISMATCH)) {
//            log.info("Failed To Send FCM Notification. FCM Service Configuration or Permission Error");
//            throw new ErrorHandler(ErrorStatus.FCM_UNAUTHORIZED);
//        }
//    }
//
//    @Recover
//    private void recover(RuntimeException exception, String memberId, Message message) {
//        throw new ErrorHandler(ErrorStatus.NOTIFY_SEND_FAIL);
//    }
//}