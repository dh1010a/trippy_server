package com.example.server.domain.notify.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.domain.Notify;
import com.example.server.domain.notify.model.NotificationType;

public class NotifyDtoConverter {

    public static NotifyDto.NotifyRequestDto convertToFollowNotifyRequestDto(Member member, Member followingMember) {
        return NotifyDto.NotifyRequestDto.builder()
                .receiver(followingMember)
                .senderProfileImgAccessUri(member.getProfileImageAccessUri())
                .senderNickName(member.getNickName())
                .senderMemberId(member.getMemberId())
                .notificationType(NotificationType.FOLLOW)
                .build();
    }

    public static NotifyDto.Response convertToResponseDto(Notify notify) {
        return NotifyDto.Response.builder()
                .notifyId(notify.getId())
                .title(notify.getTitle())
                .content(notify.getContent())
                .senderProfileImgAccessUri(notify.getSenderProfileImgAccessUri())
                .senderNickName(notify.getSenderNickName())
                .senderMemberId(notify.getSenderMemberId())
                .postId(notify.getPostId())
                .postTitle(notify.getPostTitle())
                .isRead(notify.getIsRead())
                .notificationType(notify.getNotificationType().name())
                .createdAt(notify.getCreatedAt().toString())
                .build();
    }

}
