package com.example.server.domain.post.repository;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.model.PostType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findAllByMemberAndPostType(Member member,PostType type,Sort sort);

    Page<Post> findAllByMemberAndPostType(Member member,PostType type, Pageable pageable);

    List<Post> findAllByPostType(PostType type, Sort sort);

    Page<Post> findAllByPostType(PostType type,Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.postType = :postType")
    Page<Post> findPostByTitle(@Param("keyword") String keyword, @Param("postType") PostType postType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.body LIKE %:keyword%) AND p.postType = :postType")
    Page<Post> findPostBodyAndTitle(@Param("keyword") String keyword, @Param("postType") PostType postType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.postType = :postType")
    List<Post> findPostByTitle(@Param("keyword") String keyword, @Param("postType") PostType postType);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.body LIKE %:keyword%) AND p.postType = :postType")
    List<Post> findPostBodyAndTitle(@Param("keyword") String keyword, @Param("postType") PostType postType);

    //    Page<Post> findAllByPostTypeAndSort(Pageable pageable);

    long countByPostType(PostType postType);
    long countByMemberAndPostType(Member member, PostType postType);
}
