package com.example.server.domain.notify.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class NotifyResponseDto {

    @Data
    @Builder
    public static class NotifyInfoDto {
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

    @Data
    @Builder
    public static class NotifyInfoListDto {
        int notifyCnt;
        List<NotifyInfoDto> notifyInfoList;
    }
}
