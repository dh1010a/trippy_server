package com.example.server.domain.post.domain;

import com.example.server.domain.comment.domain.Comment;
import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.BookMark;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Scope;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.ticket.domain.MemberTicket;
import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String body;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    private String location;

    private int viewCount;

    private Double score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<BookMark> bookMarks;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Like> likes;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Image> images;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Tag> tag;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments;

//    @OneToOne(mappedBy = "post")
//    private MemberTicket memberTicket;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ootd_id")
    private Ootd ootd;

    // POST 유효성 검사 - ootd, ticket 과의 관계가 있어야함
    @PrePersist
    @PreUpdate
    private void validateAssociations() {
        if (this.postType == PostType.POST) {
            if (this.ticket == null) {
                throw new IllegalStateException("POST type must have a ticket");
            }
        } else if (this.postType == PostType.OOTD) {
            if (this.ootd == null) {
                throw new IllegalStateException("OOTD type must have an ootd");
            }
        }
    }

    public void updatePost(PostRequestDto.UpdatePostRequestDto requestDto){
        this.title = requestDto.getTitle();
        this.body = requestDto.getBody();
        this.postType = requestDto.getPostType();
        this.location = requestDto.getLocation();


    }

    public void updateTags(List<Tag> newTags) {

        this.tag = newTags;
    }

    public void updateImages(List<Image> newImages) {
        this.images = newImages;
    }

    public void updateTicket(Ticket ticket){
        this.ticket = ticket;
        ticket.getImage().setPost(this);
    }

    public void updateOotd(Ootd ootd){
        this.ootd = ootd;
    }

    public LocalDateTime getCreateDate() {
        return this.getCreatedAt();
    }

    public void addViewCount(){
        this.viewCount++;
    }

}

