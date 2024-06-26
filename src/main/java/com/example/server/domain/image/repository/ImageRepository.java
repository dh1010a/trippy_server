package com.example.server.domain.image.repository;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image,Long> {
    List<Image> findAllByPost(Post post);
    Image findByImgUrlAndPost(String url,Post post);

    List<Image> findAllByPostAndImageType(Post post, ImageType imageType);
}
