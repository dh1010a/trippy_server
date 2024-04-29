package com.example.server.domain.blog.repository;

import com.example.server.domain.blog.domain.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    boolean existsByName(String name);
}
