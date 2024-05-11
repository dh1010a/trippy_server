package com.example.server.domain.post.domain;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.BookMark;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private Member member;

    @OneToMany(mappedBy = "post")
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

}

