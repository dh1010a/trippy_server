package com.example.server;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
public class PostTests {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;


    @Test
    public void insertBoard() {

        IntStream.rangeClosed(1, 100).forEach(i -> {
            Member member = memberRepository.findByEmail("user" + i + "@aaa.com");
            Post post = Post.builder()
                    .title("title" + i)
                    .body("body" + i)
                    .member(member)
                    .build();
            postRepository.save(post);
        });
    }

}
