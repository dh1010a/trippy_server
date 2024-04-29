package com.example.server.domain.post.repository;

import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag,Long> {

}
