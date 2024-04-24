package com.example.server.domain.member.domain;

//import com.example.server.domain.badge.domain.MemberBadge;
//import com.example.server.domain.follow.domain.MemberFollow;
//import com.example.server.domain.image.domain.Image;
//import com.example.server.domain.member.model.ActiveState;
//import com.example.server.domain.member.model.Gender;
//import com.example.server.domain.member.model.Role;
//import com.example.server.domain.post.domain.Like;
//import com.example.server.domain.post.domain.Post;
//import com.example.server.domain.ticket.domain.MemberTicket;
//import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.member.model.ActiveState;
import com.example.server.domain.member.model.Gender;
import com.example.server.domain.member.model.Role;
import com.example.server.global.common.BaseTimeEntity;
import com.example.server.global.security.model.ProviderType;
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
    @Column(name = "member_idx")
    private Long idx;

    private String memberId;

    private String name;

    private String password;

    private String nickName;

//    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String profileImageUrl;

    private LocalDateTime birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 1000)
    private String refreshToken;
//
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<Post> posts;
//
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<BookMark> bookMarks;
//
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<Like> likes;
//
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<MemberBadge> memberBadges;
////
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<MemberFollow> memberFollows;
//
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<MemberTicket> memberTickets;
//
//    @OneToOne
//    @JoinColumn(name = "image_id")
//    private Image profileImage;
//
//    @OneToOne
//    @JoinColumn(name = "blog_id")
//    private Blog blog;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private ActiveState activeState;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

    public void setProviderType(ProviderType type) {
        this.providerType = type;
    }

    public void updateProfileImgUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }






}
