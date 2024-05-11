package com.example.server;


import com.example.server.domain.comment.domain.Comment;
import com.example.server.domain.comment.dto.CommentDtoConverter;
import com.example.server.domain.comment.dto.CommentResponseDto;
import com.example.server.domain.comment.repository.CommentRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CommentTests {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository  postRepository;
    @Autowired
    private CommentDtoConverter commentDtoConverter;


    @Transactional
    @Test
    public void testGetComment() {
        Post post = postRepository.findById(113L).get();
        List<Comment> comments = commentRepository.findByPostId(113L);
      ;
        for (Comment comment : comments) {
            System.out.println("Comment ID: " + comment.getId());
            System.out.println("Content: " + comment.getContent());
            System.out.println("Member: " + comment.getMember().getIdx()); // 회원의 고유 식별자 출력
            System.out.println("Post: " + comment.getPost().getId()); // 게시물의 고유 식별자 출력
            // 부모 댓글이 있다면 해당 부모 댓글의 ID 출력
            if (comment.getParent() != null) {
                System.out.println("Parent Comment ID: " + comment.getParent().getId());
            }
            // 자식 댓글의 개수 출력
            if(comment.getChildComments()!=null){
                for(Comment  childComment: comment.getChildComments()){
                    System.out.println("Child Comment ID: " + childComment.getId());
                    System.out.println("Content: " + childComment.getContent());
                    System.out.println("--------------------------------------------");
                }

            }

        }



    }
    // 게시글의 댓글 전체 가져오기

}
