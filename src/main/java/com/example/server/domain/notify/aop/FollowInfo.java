//package com.example.server.domain.notify.aop;
//
//import com.example.server.domain.member.domain.Member;
//import com.example.server.domain.notify.domain.Notify;
//import com.example.server.domain.notify.model.NotificationType;
//import com.example.server.global.common.BaseTimeEntity;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Getter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString(exclude = "follow")
//public class FollowInfo extends BaseTimeEntity implements NotifyInfo {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long followRequestId;
//
//    @Column(length = 100, nullable = false)
//    private String title;
//
//
//    @Override
//    public Member getReceiver() {
//        return memberFollow.getMember();
//    }
//
//    @Override
//    public Long getGoUrlId() {
//        return memberFollow.getYataId();
//    }
//
//    @Override
//    public NotificationType getNotificationType() {
//        return NotificationType.FOLLOW;
//    }
//}
