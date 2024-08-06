package com.example.server.domain.follow.repository;

import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberFollowRepository extends JpaRepository<MemberFollow, Long> {

    public List<MemberFollow> findByFollowingMemberIdx(Long followingMemberIdx);

    @Query("SELECT mf.member.idx FROM MemberFollow mf WHERE mf.followingMemberIdx = :followingMemberIdx")
    List<Long> findFollowerList(@Param("followingMemberIdx") Long followingMemberIdx);

    @Query("SELECT mf.followingMemberIdx FROM MemberFollow mf WHERE mf.member.idx = :memberIdx")
    List<Long> findFollowingList(@Param("memberIdx") Long memberIdx);

    public List<MemberFollow> findByMemberIdx(Long memberIdx);

    public boolean existsByMemberAndFollowingMemberIdx(Member member, Long followingMemberIdx);

    public void deleteByMemberAndFollowingMemberIdx(Member member, Long followingMemberIdx);


}
