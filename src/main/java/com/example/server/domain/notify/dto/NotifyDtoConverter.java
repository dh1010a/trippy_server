package com.example.server.domain.notify.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.domain.Notify;
import com.example.server.domain.notify.model.NotificationType;

import java.util.List;

public class NotifyDtoConverter {

    public static NotifyDto.NotifyPublishRequestDto convertToFollowNotifyRequestDto(Member member, Member followingMember) {
        return NotifyDto.NotifyPublishRequestDto.builder()
                .receiver(followingMember)
                .senderProfileImgUri(member.getProfileImageAccessUri())
                .senderNickName(member.getNickName())
                .senderMemberId(member.getMemberId())
                .notificationType(NotificationType.FOLLOW)
                .build();
    }

    public static NotifyResponseDto.NotifyInfoDto convertToInfoResponseDto(Notify notify) {
        return NotifyResponseDto.NotifyInfoDto.builder()
                .notifyId(notify.getId())
                .title(notify.getTitle())
                .content(notify.getContent())
                .senderProfileImgUri(notify.getSenderProfileImgUri())
                .senderNickName(notify.getSenderNickName())
                .senderMemberId(notify.getSenderMemberId())
                .postId(notify.getPostId())
                .postTitle(notify.getPostTitle())
                .isRead(notify.getIsRead())
                .notificationType(notify.getNotificationType().name())
                .createdAt(notify.getCreatedAt().toString())
                .build();
    }

    public static NotifyResponseDto.NotifyInfoListDto convertToInfoListResponseDto(List<Notify> notifyList) {
        List<NotifyResponseDto.NotifyInfoDto> list = notifyList.stream()
                .map(NotifyDtoConverter::convertToInfoResponseDto)
                .toList();
        return NotifyResponseDto.NotifyInfoListDto.builder()
                .notifyCnt(list.size())
                .notifyInfoList(list)
                .build();

    }

}
