package com.example.server.domain.notify.repository;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.notify.domain.Notify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotifyRepository extends JpaRepository<Notify, Long>{
    List<Notify> findByReceiverOrderByCreatedAtDesc(Member member);
}
