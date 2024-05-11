package com.example.server.domain.member.domain;

//import com.example.server.domain.badge.domain.MemberBadge;
//import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.image.domain.Image;
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
import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    private String password;

    private String nickName;

//    @Column(nullable = false, unique = true)
    private String email;

    private String profileImageUrl;

    private String blogName;

    private String blogTitleImgUrl;

    private String blogIntroduce;

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

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<MemberFollow> memberFollows;
//
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<MemberTicket> memberTickets;
//
    @OneToOne
    @JoinColumn(name = "image_id")
    private Image profileImage;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private ActiveState activeState;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

    public void setSocialType(SocialType type) {
        this.socialType = type;
    }

    public void updateProfileImgUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateMemberFollowing(MemberFollow memberFollow) {
        this.memberFollows.add(memberFollow);
    }

    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    public void updateBlogName(String blogName) {
        this.blogName = blogName;
    }

    public void updateBlogTitleImgUrl(String blogTitleImgUrl) {
        this.blogTitleImgUrl = blogTitleImgUrl;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateBlogIntroduce(String blogIntroduce) {
        this.blogIntroduce = blogIntroduce;
    }






}
