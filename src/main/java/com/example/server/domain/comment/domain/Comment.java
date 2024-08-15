package com.example.server.domain.comment.domain;

import com.example.server.domain.comment.model.CommentStatus;
import com.example.server.domain.comment.model.DeleteStatus;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Scope;
import com.example.server.domain.post.domain.Post;
import com.example.server.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.sql.Delete;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="post_comment")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private String content;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private Scope status = Scope.PUBLIC;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "deleteStatus")
    @Builder.Default
    private DeleteStatus deleteStatus = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Comment> childComments = new ArrayList<>();  // 수정된 부분


    public void updateParent(Comment parentComment){
        this.parent = parentComment;
    }

    public void updateStatus(Scope status){
        this.status = status;
    }
    public void updateDeleteStatus(DeleteStatus deleteStatus){this.deleteStatus = deleteStatus;}

    public void updateContent(String content){
        this.content = content;
    }

    public void deleteMember(){
        this.member = null;
    }

    public LocalDateTime getCreateDate() {
        return this.getCreatedAt();
    }


}
