package com.example.server.domain.notify.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.model.NotificationType;
import lombok.Builder;
import lombok.Data;

public class NotifyRequestDto {

    @Data
    @Builder
    public static class PublishNotifyRequestDto {
        Member receiver;
        String content;
        String senderProfileImgUri;
        String senderNickName;
        String senderMemberId;
        Long postId;
        String postTitle;
        NotificationType notificationType;
    }

}
