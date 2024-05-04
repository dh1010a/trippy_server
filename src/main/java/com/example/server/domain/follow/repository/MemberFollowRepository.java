package com.example.server.domain.follow.repository;

import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberFollowRepository extends JpaRepository<MemberFollow, Long> {

    public List<MemberFollow> findByFollowingMemberIdx(Long followingMemberIdx);

    public boolean existsByMemberAndFollowingMemberIdx(Member member, Long followingMemberIdx);
}
