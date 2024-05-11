package com.example.server.domain.post.repository;

import com.example.server.domain.post.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Long> {

    @Query("SELECT COUNT(*) FROM Like like WHERE like.post.id = :postId")
    Integer countByPostId(@Param("postId") Long postId);

    Optional<Like> findByPostIdAndMemberIdx(Long postId, Long memberIdx);
}
