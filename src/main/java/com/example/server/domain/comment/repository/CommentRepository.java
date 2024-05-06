package com.example.server.domain.comment.repository;

import com.example.server.domain.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CommentRepository extends JpaRepository<Comment,Long> {


    List<Comment> findByPostId(Long postId);
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.id ASC")
    List<Comment> findByPostIdOrderByAsc(@Param("postId") Long postId);

}
