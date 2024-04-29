package com.example.server.domain.blog.domain;

import com.example.server.domain.member.domain.Member;
import com.example.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    private Long id;

    private String name;

    private String introduce;


    @OneToOne
    @JoinColumn(name = "member_idx")
    private Member member;

}
