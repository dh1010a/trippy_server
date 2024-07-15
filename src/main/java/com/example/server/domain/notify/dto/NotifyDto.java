package com.example.server.domain.notify.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.model.NotificationType;
import lombok.*;

public class NotifyDto {


    @Data
    @Builder
    public static class NotifyRequestDto {
        Member receiver;
        String content;
        String senderProfileImgUri;
        String senderNickName;
        String senderMemberId;
        Long postId;
        String postTitle;
        NotificationType notificationType;
    }


    @Data
    @Builder
    public static class Response {
        Long notifyId;
        String title;
        String content;
        String senderProfileImgUri;
        String senderNickName;
        String senderMemberId;
        Long postId;
        String postTitle;
        boolean isRead;
        String notificationType;
        String createdAt;
    }
}
