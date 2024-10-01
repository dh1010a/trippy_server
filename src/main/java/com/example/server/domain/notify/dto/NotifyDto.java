package com.example.server.domain.notify.dto;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.model.NotificationType;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.model.PostType;
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
        PostType postType;

        public void updatePostInfo(Post post) {
            this.postId = post.getId();
            this.postTitle = post.getTitle();
            this.postType = post.getPostType();
        }
    }

}
