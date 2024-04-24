package com.example.server.domain.member.repository;

import com.example.server.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByRefreshToken(String refreshToken);

    boolean existsByNickName(String nickName);
    boolean existsByEmail(String nickName);

    boolean existsByMemberId(String memberId);
}
