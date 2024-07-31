package com.example.server.domain.post.repository;

import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag,Long> {
    List<Tag> findAllByPost(Post post);
    Tag findByNameAndPost(String tagName,Post post);

    @Query("SELECT DISTINCT t.post FROM Tag t WHERE t.name = :name")
    List<Post> findPostsByTag(@Param("name") String name);

    @Query("SELECT DISTINCT t.post FROM Tag t WHERE t.name = :name")
    Page<Post> findPostsByTag(@Param("name") String name, Pageable pageable);
  //  List<Tag> findAllByPost(Post post);


}
