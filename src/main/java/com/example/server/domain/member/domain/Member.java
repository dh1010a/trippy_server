package com.example.server.domain.member.domain;

import com.example.server.domain.badge.domain.MemberBadge;
import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.model.ActiveState;
import com.example.server.domain.member.model.Gender;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.post.domain.Like;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.ticket.domain.MemberTicket;
import com.example.server.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String name;


    private String password;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String profileImageUrl;

    private LocalDateTime birthDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<Post> posts;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<BookMark> bookMarks;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<Like> likes;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<MemberBadge> memberBadges;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<MemberFollow> memberFollows;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<MemberTicket> memberTickets;

    @OneToOne
    @JoinColumn(name = "image_id")
    private Image profileImage;

    @OneToOne
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private ActiveState activeState;


}
