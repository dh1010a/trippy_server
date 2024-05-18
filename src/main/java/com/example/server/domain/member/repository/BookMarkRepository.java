package com.example.server.domain.member.repository;

import com.example.server.domain.member.domain.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark,Long> {
}
