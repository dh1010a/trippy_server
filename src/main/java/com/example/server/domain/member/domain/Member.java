package com.example.server.domain.member.domain;

//import com.example.server.domain.badge.domain.MemberBadge;
//import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.comment.domain.Comment;
import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.image.domain.Image;
//import com.example.server.domain.member.model.ActiveState;
//import com.example.server.domain.member.model.Gender;
//import com.example.server.domain.member.model.Role;
//import com.example.server.domain.post.domain.Like;
//import com.example.server.domain.post.domain.Post;
//import com.example.server.domain.ticket.domain.MemberTicket;
//import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.member.model.*;
import com.example.server.domain.notify.domain.Notify;
import com.example.server.domain.post.domain.Post;
import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(nullable = false, unique = true)
    private String nickName;

//    @Column(nullable = false, unique = true)
    private String email;

    private String blogName;

    private String blogIntroduce;

    @Column(length = 1000)
    private String refreshToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Post> posts;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<BookMark> bookMarks;

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

    @Enumerated(EnumType.STRING)
    private Scope ticketScope;

    @Enumerated(EnumType.STRING)
    private Scope ootdScope;

    @Enumerated(EnumType.STRING)
    private Scope badgeScope;

    @Enumerated(EnumType.STRING)
    private Scope followScope;
//
//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private List<MemberTicket> memberTickets;
//
    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private List<Image> images;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private ActiveState activeState;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private int followerCnt;

    private int followingCnt;

    private boolean likeAlert;

    private boolean commentAlert;

    private LocalDateTime lastImageUploadBlockedTime;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<InterestedType> interestedTypes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Notify> notifies;

    public void initDefaultSetting() {
        this.ticketScope = Scope.PUBLIC;
        this.ootdScope = Scope.PUBLIC;
        this.badgeScope = Scope.PUBLIC;
        this.followScope = Scope.PUBLIC;
        this.followerCnt = 0;
        this.followingCnt = 0;
        this.likeAlert = true;
        this.commentAlert = true;
    }

    public String getProfileImageAccessUri() {
        Image image = images.stream().filter(Image::isProfileImage).findAny().orElse(null);
        return image != null ? image.getAccessUri() : null;
    }

    public String getBlogTitleImageAccessUri() {
        Image image = images.stream().filter(Image::isBlogTitleImage).findAny().orElse(null);
        return image != null ? image.getAccessUri() : null;
    }


    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateLastImageUploadBlockedTime(LocalDateTime lastImageUploadBlockedTime) {
        this.lastImageUploadBlockedTime = lastImageUploadBlockedTime;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

    public void setSocialType(SocialType type) {
        this.socialType = type;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public void updateBlogIntroduce(String blogIntroduce) {
        this.blogIntroduce = blogIntroduce;
    }
    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateInfo(String nickName, String blogName, String blogIntroduce) {
        this.nickName = nickName;
        this.blogName = blogName;
        this.blogIntroduce = blogIntroduce;
    }

    public void increaseFollowerCnt() {
        this.followerCnt++;
    }

    public void decreaseFollowerCnt() {
        this.followerCnt--;
    }

    public void increaseFollowingCnt() {
        this.followingCnt++;
    }

    public void decreaseFollowingCnt() {
        this.followingCnt--;
    }

    public void updateAlert(boolean likeAlert, boolean commentAlert) {
        this.likeAlert = likeAlert;
        this.commentAlert = commentAlert;
    }

    public void updateInterestedTypes(List<String> interestedTypes) {
        this.interestedTypes.clear();
        interestedTypes.forEach(interestedType -> this.interestedTypes.add(InterestedType.fromKoreanName(interestedType)));
    }

    public void setInterestedTypes(List<InterestedType> interestedTypes) {
        this.interestedTypes = interestedTypes;
    }

    public void updateScope(Scope ticketScope, Scope ootdScope, Scope badgeScope, Scope followScope) {
        this.ticketScope = ticketScope;
        this.ootdScope = ootdScope;
        this.badgeScope = badgeScope;
        this.followScope = followScope;
    }





}
