package com.example.server.domain.badge.domain;

import com.example.server.domain.member.domain.Member;
import com.example.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "MemberBadge")
public class MemberBadge  extends BaseTimeEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="memberBadge_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_idx")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "badge_id")
    private Badge badge;


}
