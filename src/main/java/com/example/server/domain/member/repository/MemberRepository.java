package com.example.server.domain.member.repository;

import com.example.server.domain.member.domain.Member;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.oauth2.model.SocialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByIdx(Long idx);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickName(String nickName);
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByRefreshToken(String refreshToken);
    Optional<Member> findBySocialTypeAndMemberId(SocialType socialType, String memberId);

    boolean existsByBlogName(String blogName);
    boolean existsByNickName(String nickName);
    boolean existsByEmail(String nickName);

    void deleteByMemberId(String memberId);

    boolean existsByMemberId(String memberId);

    default Member getMemberById(String memberId) {
        return findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
    default Member getMemberByNickName(String nickName) {
        return findByNickName(nickName).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Query("SELECT m FROM Member m WHERE m.nickName LIKE %:keyword%")
    Page<Member> findByNicknameContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.blogName LIKE %:keyword%")
    Page<Member> findByBlogNameContaining(@Param("keyword") String keyword, Pageable pageable);
}
