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

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "LEFT JOIN p.comments c " +
            "LEFT JOIN p.member m " +
            "LEFT JOIN MemberFollow mf ON m.idx = mf.followingMemberIdx " +
            "WHERE p.member = :member " +
            "AND p.postType = :postType " +
            "AND ( " +
            "      (p.member.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (p.member.ticketScope = 'PROTECTED' AND :postType = 'POST' AND mf.member.idx = :loginMemberId) OR " +
            "      (p.member.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND mf.member.idx = :loginMemberId)" +
            ") " +
            "AND ( " +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ") " +
            "GROUP BY p.id")
    Page<Post> findAllByMemberAndPostType(
            @Param("member") Member member,
            @Param("postType") PostType postType,
            @Param("loginMemberId") Long loginMemberId,
            Pageable pageable);


    List<Post> findAllByPostType(PostType type, Sort sort);

    Page<Post> findAllByPostType(PostType type,Pageable pageable);

    // 좋아요 순 정렬
    @Query("SELECT p FROM Post p LEFT JOIN p.likes l LEFT JOIN p.comments c WHERE p.postType = :postType GROUP BY p.id ORDER BY COUNT(l) DESC, COUNT(c) DESC")
    Page<Post> findAllByPostTypeOrderByLikeCountDesc(@Param("postType") PostType postType, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "LEFT JOIN p.comments c " +
            "LEFT JOIN p.member m " +
            "LEFT JOIN MemberFollow mf ON m.idx = mf.followingMemberIdx " +
            "WHERE p.postType = :postType " +
            "AND p.member = :member " +
            "AND ( " +
            "      (p.member.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (p.member.ticketScope = 'PROTECTED' AND :postType = 'POST' AND mf.member.idx = :loginMemberId) OR " +
            "      (p.member.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND mf.member.idx = :loginMemberId)" +
            ") " +
            "AND ( " +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ") " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(l.id) DESC, COUNT(c.id) DESC")
    Page<Post> findAllByPostTypeAndMemberOrderByLikeCountDesc( @Param("postType") PostType postType,
                                                               @Param("member") Member member,
                                                               @Param("loginMemberId") Long loginMemberId,
                                                               Pageable pageable);


    // 제목 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.postType = :postType")
    Page<Post> findPostByTitle(@Param("keyword") String keyword, @Param("postType") PostType postType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.postType = :postType")
    List<Post> findPostByTitle(@Param("keyword") String keyword, @Param("postType") PostType postType);

    // 제목+내용 검색
    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.body LIKE %:keyword%) AND p.postType = :postType")
    Page<Post> findPostBodyAndTitle(@Param("keyword") String keyword, @Param("postType") PostType postType, Pageable pageable);


    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.body LIKE %:keyword%) AND p.postType = :postType")
    List<Post> findPostBodyAndTitle(@Param("keyword") String keyword, @Param("postType") PostType postType);

    // 게시물 전체 조회 (공개범위 적용) -> 인기순 정렬
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "LEFT JOIN p.comments c " +
            "LEFT JOIN p.member m " +
            "LEFT JOIN MemberFollow mf ON m.idx = mf.followingMemberIdx " +
            "WHERE p.postType = :postType " +
            "AND ( " +
            "      (p.member.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (p.member.ticketScope = 'PROTECTED' AND :postType = 'POST' AND mf.member.idx = :loginMemberId) OR " +
            "      (p.member.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND mf.member.idx = :loginMemberId)" +
            ") " +
            "AND ( " +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ") " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(l.id) DESC, COUNT(c.id) DESC")
    Page<Post> findAllByPostTypeWithScopeAndOrderLike(
            @Param("postType") PostType postType,
            @Param("loginMemberId") Long loginMemberId,
            Pageable pageable);
    ;

    // 게시물 전체 조회 (공개범위 적용) -> 최신순, 조회수 정렬
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "LEFT JOIN p.comments c " +
            "LEFT JOIN p.member m " +
            "LEFT JOIN MemberFollow mf ON m.idx = mf.followingMemberIdx " +
            "WHERE p.postType = :postType " +
            "AND (p.member.ticketScope = 'PUBLIC' OR " +
            "      p.member.ootdScope = 'PUBLIC' OR " +
            "      (:postType = 'POST' AND mf.member.idx = :loginMemberId AND p.member.ticketScope = 'PROTECTED') OR " +
            "      (:postType = 'OOTD' AND mf.member.idx = :loginMemberId AND p.member.ootdScope = 'PROTECTED')) " +
            "AND ( " +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ") " +
            "GROUP BY p.id")
    Page<Post> findAllByPostTypeWithScope(
            @Param("postType") PostType postType,
            @Param("loginMemberId") Long loginMemberId,
            Pageable pageable);


    //    Page<Post> findAllByPostTypeAndSort(Pageable pageable);

    long countByPostType(PostType postType);
    long countByMemberAndPostType(Member member, PostType postType);

    // 팔로잉한 게시물
    Page<Post> findByMemberIdxInAndPostType(List<Long> memberIds, PostType postType, Pageable pageable);
}
