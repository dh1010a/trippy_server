package com.example.server.domain.notify.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.notify.domain.Notify;
import com.example.server.domain.notify.domain.NotifyMessageProvider;
import com.example.server.domain.notify.dto.NotifyDto.NotifyPublishRequestDto;
import com.example.server.domain.notify.dto.NotifyDtoConverter;
import com.example.server.domain.notify.dto.NotifyResponseDto;
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
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class NotifyService {

    private final NotifyRepository notifyRepository;
    private final EmitterRepository emitterRepository;

    // 1시간
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final MemberRepository memberRepository;

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

    public void sendNotify(Member receiver, NotifyPublishRequestDto notifyPublishRequestDto) {
        Notify notification = notifyRepository.save(createNotify(receiver, notifyPublishRequestDto));

        String memberId = receiver.getMemberId();
        String eventId = memberId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(memberId);
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotifyDtoConverter.convertToInfoResponseDto(notification));
                }
        );
    }

    // GET /api/notify
    public NotifyResponseDto.NotifyInfoListDto getAllNotify(String memberId) {
        Member member = memberRepository.getMemberById(memberId);
        List<Notify> notifyList = notifyRepository.findByReceiverOrderByCreatedAtDesc(member);
        return NotifyDtoConverter.convertToInfoListResponseDto(notifyList);
    }

    // GET[ADMIN] /api/admin/notify/all
    public NotifyResponseDto.NotifyInfoListDto getAllMemberNotify() {
        List<Notify> notifyList = notifyRepository.findAll();
        return NotifyDtoConverter.convertToInfoListResponseDto(notifyList);
    }

    // POST /api/notify/read
    public void readNotify(String memberId, Long notifyId) {
        Notify notify = notifyRepository.findById(notifyId).orElseThrow(() -> new ErrorHandler(ErrorStatus.NOTIFY_NOT_FOUND));
        if (!notify.getReceiver().getMemberId().equals(memberId)) {
            throw new ErrorHandler(ErrorStatus.NOTIFY_NOT_FOUND);
        }
        notify.readNotify();
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

    private Notify createNotify(Member receiver, NotifyPublishRequestDto notifyPublishRequestDto) {
        NotificationType notificationType = notifyPublishRequestDto.getNotificationType();
        return switch (notificationType) {
            case FOLLOW -> Notify.builder()
                    .receiver(receiver)
                    .title(NotifyMessageProvider.getNewFollowerMessage(notifyPublishRequestDto.getSenderNickName()))
                    .senderProfileImgUri(notifyPublishRequestDto.getSenderProfileImgUri())
                    .senderNickName(notifyPublishRequestDto.getSenderNickName())
                    .senderMemberId(notifyPublishRequestDto.getSenderMemberId())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            case LIKE -> Notify.builder()
                    .receiver(receiver)
                    .title(NotifyMessageProvider.getNewLikeMessage(notifyPublishRequestDto.getSenderNickName()))
                    .senderProfileImgUri(notifyPublishRequestDto.getSenderProfileImgUri())
                    .senderNickName(notifyPublishRequestDto.getSenderNickName())
                    .senderMemberId(notifyPublishRequestDto.getSenderMemberId())
                    .postId(notifyPublishRequestDto.getPostId())
                    .postTitle(notifyPublishRequestDto.getPostTitle())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            case COMMENT -> Notify.builder()
                    .receiver(receiver)
                    .title(NotifyMessageProvider.getNewCommentMessage(notifyPublishRequestDto.getSenderNickName()))
                    .senderProfileImgUri(notifyPublishRequestDto.getSenderProfileImgUri())
                    .senderNickName(notifyPublishRequestDto.getSenderNickName())
                    .senderMemberId(notifyPublishRequestDto.getSenderMemberId())
                    .content(notifyPublishRequestDto.getContent())
                    .postId(notifyPublishRequestDto.getPostId())
                    .postTitle(notifyPublishRequestDto.getPostTitle())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            default -> throw new ErrorHandler(ErrorStatus.NOTIFY_UNSUPPORTED_TYPE);
        };
    }
}
