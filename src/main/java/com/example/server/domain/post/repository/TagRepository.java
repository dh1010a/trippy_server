package com.example.server.domain.post.repository;

import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag,Long> {
    List<Tag> findAllByPost(Post post);
    Tag findByNameAndPost(String tagName,Post post);
  //  List<Tag> findAllByPost(Post post);


}
