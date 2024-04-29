package com.example.server.domain.follow.repository;

import com.example.server.domain.follow.domain.MemberFollow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberFollowRepository extends JpaRepository<MemberFollow, Long> {
}
