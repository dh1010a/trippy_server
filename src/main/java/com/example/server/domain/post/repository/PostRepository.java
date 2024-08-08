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

    // 특정 멤버의 게시물 -> 공개 범위 적용
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "LEFT JOIN p.comments c " +
            "LEFT JOIN p.member m " +
            "WHERE p.member = :member " +
            "AND p.postType = :postType " +
            "AND ( " +
            "      (p.member.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (p.member.ticketScope = 'PROTECTED' AND :postType = 'POST' AND p.member.idx IN :followingList) OR " +
            "      (p.member.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND p.member.idx IN :followingList)" +
            ") " +
            "AND ( " +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ") " +
            "GROUP BY p.id")
    Page<Post> findAllByMemberAndPostType(@Param("postType") PostType postType,
    @Param("member") Member member,
    @Param("followingList") List<Long> followingList,
    Pageable pageable);

    // 특정 멤버 게시물 개수 -> 공개 범위 적용
    @Query("SELECT COUNT(p) FROM Post p " +
            "WHERE p.member = :member " +
            "AND p.postType = :postType " +
            "AND (" +
            "      (p.member.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (p.member.ticketScope = 'PROTECTED' AND :postType = 'POST' AND p.member.idx IN :followingList) OR " +
            "      (p.member.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND p.member.idx IN :followingList)" +
            ") " +
            "AND (" +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ")")
    long countByMemberAndPostTypeWithScope(
            @Param("member") Member member,
            @Param("postType") PostType postType,
            @Param("followingList") List<Long> followingList);


    // 게시물 전체 조회 (공개 범위 적용)
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "LEFT JOIN p.comments c " +
            "LEFT JOIN p.member m " +
            "WHERE p.postType = :postType " +
            "AND ( " +
            "      (p.member.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (p.member.ticketScope = 'PROTECTED' AND :postType = 'POST' AND p.member.idx IN :followingList) OR " +
            "      (p.member.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND p.member.idx IN :followingList)" +
            ") " +
            "AND ( " +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ") " +
            "GROUP BY p.id")
    Page<Post> findAllByPostTypeWithScope(
            @Param("postType") PostType postType,
            @Param("followingList") List<Long> followingList,
            Pageable pageable);

    // 게시물 전체 개수 -> 공개 범위 적용
    @Query("SELECT COUNT(p) FROM Post p " +
            "WHERE p.postType = :postType " +
            "AND (" +
            "      (p.member.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (p.member.ticketScope = 'PROTECTED' AND :postType = 'POST' AND p.member.idx IN :followingList) OR " +
            "      (p.member.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND p.member.idx IN :followingList)" +
            ") " +
            "AND (" +
            "      (p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ")")
    long countByPostTypeWithScope(@Param("postType") PostType postType, @Param("followingList") List<Long> followingList);

    // 내 게시물 전체 개수
    long countByMemberAndPostType(Member member, PostType postType);

    // 내 게시물 -> 최신순, 조회수 정렬
    @Query("SELECT p FROM Post p WHERE p.member = :member AND p.postType = :postType")
    Page<Post> findMyPostsWithScore(
                            @Param("member") Member member,
                           @Param("postType") PostType postType,
                           Pageable pageable);
    // 제목 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.postType = :postType")
    Page<Post> findPostByTitle(@Param("keyword") String keyword, @Param("postType") PostType postType, Pageable pageable);

    // 제목 검색
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.member m " +
            "WHERE p.title LIKE %:keyword% " +
            "AND p.postType = :postType " +
            "AND ( " +
            "      (m.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (m.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (m.ticketScope = 'PROTECTED' AND :postType = 'POST' AND m.idx IN :followingList) OR " +
            "      (m.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND m.idx IN :followingList)" +
            ") " +
            "AND ( " +
            "      (m.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (m.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ")")
    Page<Post> findPostByTitle(@Param("keyword") String keyword,
                               @Param("postType") PostType postType,
                               @Param("followingList") List<Long> followingList,
                               Pageable pageable);

    // 제목 + 내용 검색
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.member m " +
            "WHERE (p.title LIKE %:keyword% OR p.body LIKE %:keyword%) " +
            "AND p.postType = :postType " +
            "AND ( " +
            "      (m.ticketScope = 'PUBLIC' AND :postType = 'POST') OR " +
            "      (m.ootdScope = 'PUBLIC' AND :postType = 'OOTD') OR " +
            "      (m.ticketScope = 'PROTECTED' AND :postType = 'POST' AND m.idx IN :followingList) OR " +
            "      (m.ootdScope = 'PROTECTED' AND :postType = 'OOTD' AND m.idx IN :followingList)" +
            ") " +
            "AND ( " +
            "      (m.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (m.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ")")
    Page<Post> findPostBodyAndTitle(@Param("keyword") String keyword,
                                    @Param("postType") PostType postType,
                                    @Param("followingList") List<Long> followingList,
                                    Pageable pageable);


    // 팔로잉한 게시물
    @Query("SELECT p FROM Post p " +
            "WHERE p.member.idx IN :followingMemberIds " +
            "AND p.postType = :postType " +
            "AND ((p.member.ticketScope <> 'PRIVATE' AND :postType = 'POST') " +
            "     OR (p.member.ootdScope <> 'PRIVATE' AND :postType = 'OOTD'))")
    Page<Post> findByMemberIdxInAndPostType(@Param("followingMemberIds") List<Long> followingMemberIds,
                                            @Param("postType") PostType postType,
                                            Pageable pageable);

    long countByPostType(PostType postType);

    List<Post> findAllByPostType(PostType type, Sort sort);

    Page<Post> findAllByPostType(PostType type,Pageable pageable);


}
