package com.example.server.domain.post.repository;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.model.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findAllByMemberAndPostType(Member member,PostType type,Sort sort);

    Page<Post> findAllByMemberAndPostType(Member member,PostType type, Pageable pageable);

    List<Post> findAllByPostType(PostType type, Sort sort);

    Page<Post> findAllByPostType(PostType type,Pageable pageable);

//    Page<Post> findAllByPostTypeAndSort(Pageable pageable);

    long countByPostType(PostType postType);
    long countByMemberAndPostType(Member member, PostType postType);
}
