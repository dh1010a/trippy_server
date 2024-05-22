package com.example.server.domain.image.domain;

import com.example.server.domain.member.domain.Member;
//import com.example.server.domain.post.domain.Post;
//import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.domain.post.domain.Post;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Image")
public class Image {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToOne
    @JoinColumn(name = "member_idx")
    private Member member;


    private String imgUrl;

    public void setPost(Post post) {
        this.post = post;
    }


}
