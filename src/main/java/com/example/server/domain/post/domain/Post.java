package com.example.server.domain.post.domain;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.BookMark;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.model.PostType;
import com.example.server.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    private PostType postType;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "post")
    @JsonIgnore
    private List<BookMark> bookMarks;

    @OneToMany(mappedBy = "post")
    @JsonIgnore
    private List<Like> likes;


    @OneToMany(mappedBy = "post")
    @JsonIgnore
    private List<Image> images;

    @OneToMany(mappedBy = "post")
    @JsonIgnore
    private List<Tag> tag;

}
