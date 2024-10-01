package com.example.server.domain.notify.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.notify.domain.Notify;
import com.example.server.domain.notify.domain.NotifyMessageProvider;
import com.example.server.domain.notify.dto.NotifyDto;
import com.example.server.domain.notify.dto.NotifyDtoConverter;
import com.example.server.domain.notify.dto.NotifyResponseDto;
import com.example.server.domain.notify.model.NotificationType;
import com.example.server.domain.notify.repository.NotifyRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class NotifyService {

    private final NotifyRepository notifyRepository;
    // 1시간
    private final MemberRepository memberRepository;


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


    public Notify createNotify(Member receiver, NotifyDto.NotifyPublishRequestDto notifyPublishRequestDto) {
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
                    .postType(notifyPublishRequestDto.getPostType())
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
                    .postType(notifyPublishRequestDto.getPostType())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            case REPLY -> Notify.builder()
                    .receiver(receiver)
                    .title(NotifyMessageProvider.getNewCommentReplyMessage(notifyPublishRequestDto.getSenderNickName()))
                    .senderProfileImgUri(notifyPublishRequestDto.getSenderProfileImgUri())
                    .senderNickName(notifyPublishRequestDto.getSenderNickName())
                    .senderMemberId(notifyPublishRequestDto.getSenderMemberId())
                    .content(notifyPublishRequestDto.getContent())
                    .postId(notifyPublishRequestDto.getPostId())
                    .postTitle(notifyPublishRequestDto.getPostTitle())
                    .postType(notifyPublishRequestDto.getPostType())
                    .isRead(false)
                    .notificationType(notificationType)
                    .build();
            default -> throw new ErrorHandler(ErrorStatus.NOTIFY_UNSUPPORTED_TYPE);
        };
    }
}

