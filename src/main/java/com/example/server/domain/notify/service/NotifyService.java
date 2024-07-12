package com.example.server.domain.notify.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.domain.Notify;
import com.example.server.domain.notify.domain.NotifyMessageProvider;
import com.example.server.domain.notify.dto.NotifyDto;
import com.example.server.domain.notify.dto.NotifyDto.NotifyRequestDto;
import com.example.server.domain.notify.dto.NotifyDtoConverter;
import com.example.server.domain.notify.model.NotificationType;
import com.example.server.domain.notify.repository.EmitterRepository;
import com.example.server.domain.notify.repository.NotifyRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class NotifyService {

    private final NotifyRepository notifyRepository;
    private final EmitterRepository emitterRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter subscribe(String memberId, String lastEventId) {
        String emitterId = makeTimeIncludeId(memberId);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        String eventId = makeTimeIncludeId(memberId);
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [memberId=" + memberId + "]");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, memberId, emitterId, emitter);
        }

        return emitter;
    }

    public void sendNotify(Member receiver, NotifyRequestDto notifyRequestDto) {
        Notify notification = notifyRepository.save(createNotify(receiver, notifyRequestDto));

        String memberId = receiver.getMemberId();
        String eventId = memberId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(memberId);
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotifyDtoConverter.convertToResponseDto(notification));
                }
        );
    }

    private String makeTimeIncludeId(String memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("sse")
                    .data(data)
            );
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            emitter.completeWithError(exception);
        }
    }

    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, String memberId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByMemberId(String.valueOf(memberId));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    private Notify createNotify(Member receiver, NotifyRequestDto notifyRequestDto) {
        NotificationType notificationType = notifyRequestDto.getNotificationType();
        return switch (notificationType) {
            case FOLLOW -> Notify.builder()
                    .receiver(receiver)
                    .title(NotifyMessageProvider.getNewFollowerMessage(notifyRequestDto.getSenderNickName()))
                    .senderProfileImgAccessUri(notifyRequestDto.getSenderProfileImgAccessUri())
                    .senderNickName(notifyRequestDto.getSenderNickName())
                    .senderMemberId(notifyRequestDto.getSenderMemberId())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            case LIKE -> Notify.builder()
                    .receiver(receiver)
                    .title(NotifyMessageProvider.getNewLikeMessage(notifyRequestDto.getSenderNickName()))
                    .senderProfileImgAccessUri(notifyRequestDto.getSenderProfileImgAccessUri())
                    .senderNickName(notifyRequestDto.getSenderNickName())
                    .senderMemberId(notifyRequestDto.getSenderMemberId())
                    .postId(notifyRequestDto.getPostId())
                    .postTitle(notifyRequestDto.getPostTitle())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            case COMMENT -> Notify.builder()
                    .receiver(receiver)
                    .title(NotifyMessageProvider.getNewCommentMessage(notifyRequestDto.getSenderNickName()))
                    .senderProfileImgAccessUri(notifyRequestDto.getSenderProfileImgAccessUri())
                    .senderNickName(notifyRequestDto.getSenderNickName())
                    .senderMemberId(notifyRequestDto.getSenderMemberId())
                    .content(notifyRequestDto.getContent())
                    .postId(notifyRequestDto.getPostId())
                    .postTitle(notifyRequestDto.getPostTitle())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            default -> throw new ErrorHandler(ErrorStatus.NOTIFY_UNSUPPORTED_TYPE);
        };
    }
}
