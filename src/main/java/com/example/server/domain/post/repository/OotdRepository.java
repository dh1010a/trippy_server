package com.example.server.domain.post.repository;

import com.example.server.domain.post.domain.Ootd;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OotdRepository extends JpaRepository<Ootd,Long> {
}
