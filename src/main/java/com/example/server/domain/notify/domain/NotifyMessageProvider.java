package com.example.server.domain.notify.domain;

public class NotifyMessageProvider {
    private final static String NEW_FOLLOWER = "님이 회원님을 팔로우하기 시작했습니다.";
    private final static String NEW_COMMENT = "님이 회원님의 게시물에 댓글을 남겼습니다.";
    private final static String NEW_LIKE = "님이 회원님의 게시물을 좋아합니다.";
    private final static String NEW_COMMENT_REPLY = "님이 회원님의 댓글에 답글을 남겼습니다.";
    private final static String NEW_BADGE = "새로운 뱃지를 획득했습니다.";



    public static String getNewFollowerMessage(String nickName) {
        return nickName + NEW_FOLLOWER;
    }

    public static String getNewCommentMessage(String nickName) {
        return nickName + NEW_COMMENT;
    }

    public static String getNewLikeMessage(String nickName) {
        return nickName + NEW_LIKE;
    }

    public static String getNewCommentReplyMessage(String nickName) {
        return nickName + NEW_COMMENT_REPLY;
    }

    public static String getNewBadgeMessage() {
        return NEW_BADGE;
    }
}
