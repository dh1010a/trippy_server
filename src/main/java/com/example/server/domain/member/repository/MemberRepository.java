package com.example.server.domain.member.repository;

import com.example.server.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member,Long> {
}
