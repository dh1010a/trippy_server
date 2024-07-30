package com.example.server.domain.notify.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.model.NotificationType;
import lombok.*;

public class NotifyDto {


    // SSE 알림용 DTO

    @Data
    @Builder
    public static class NotifyPublishRequestDto {
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
