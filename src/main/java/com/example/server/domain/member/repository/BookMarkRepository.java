package com.example.server.domain.member.repository;

import com.example.server.domain.bookmark.domain.BookMark;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.model.PostType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark,Long> {

    @Query("SELECT b.post.id FROM BookMark b WHERE b.member = :member")
    List<Long> findAllByMember(@Param("member") Member member);

    // 북마크한 Post 조회
    @Query("SELECT p FROM Post p " +
            "JOIN p.bookMarks b " +
            "JOIN p.member m " +
            "WHERE b.member.memberId = :memberId " +
            "AND p.postType = :postType " +
//            "AND (" +
//            "      m.idx IN :bookMarkedList " +
//            ") " +
            "AND (" +
            "      (m.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (m.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ")")
    Page<Post> findBookMarkedPostsByMemberWithPostTypeAndScope(@Param("memberId") String memberId,
                                                               @Param("postType") PostType postType,
//                                                               @Param("bookMarkedList") List<Long> bookMarkedList,
                                                               Pageable pageable);

    // 북마크 카운트
    @Query("SELECT COUNT(p) FROM Post p " +
            "JOIN p.bookMarks b " +
            "JOIN p.member m " +
            "WHERE b.member.memberId = :memberId " +
            "AND p.postType = :postType " +
            "AND (" +
            "      (m.ticketScope <> 'PRIVATE' AND :postType = 'POST') OR " +
            "      (m.ootdScope <> 'PRIVATE' AND :postType = 'OOTD')" +
            ")")
    long countBookMarkedPostsByMemberWithPostTypeAndScope(@Param("memberId") String memberId,
                                                          @Param("postType") PostType postType);


    boolean existsByMemberAndPost(Member member, Post post);

    Optional<BookMark> findByMemberAndPost(Member member, Post post);
}
