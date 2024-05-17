package com.example.server.domain.member.repository;

import com.example.server.domain.member.domain.Member;
import com.example.server.global.auth.oauth2.model.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByIdx(Long idx);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByRefreshToken(String refreshToken);
    Optional<Member> findBySocialTypeAndMemberId(SocialType socialType, String memberId);
    Optional<Member> findByNickName(String nickName);

    boolean existsByBlogName(String blogName);
    boolean existsByNickName(String nickName);
    boolean existsByEmail(String nickName);

    void deleteByMemberId(String memberId);

    boolean existsByMemberId(String memberId);
}
