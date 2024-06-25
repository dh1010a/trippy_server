package com.example.server.domain.post.repository;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findAllByMember(Member member);

    Page<Post> findAllByMember(Member member, Pageable pageable);
}
