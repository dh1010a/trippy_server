package com.example.server.domain.notify.domain;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.model.NotificationType;
import com.example.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.domain.Auditable;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notify extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notify_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_idx")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member receiver;

    private String title;

    private String content;

    @Column(length = 1000)
    private String senderProfileImgUri;

    private String senderNickName;

    private String senderMemberId;

    private Long postId;

    private String postTitle;

    @Column(nullable = false)
    private Boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;


}
