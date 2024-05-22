package com.example.server;


import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.repository.ImageRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.domain.post.service.PostService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@Transactional
@SpringBootTest
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

   // @Test
    public void testUpdateTagsAndImages() {
        // 테스트할 데이터 생성

        Member member =  memberRepository.findByEmail("dkdud203@naver.com").get();
        Post post = postRepository.findById(113L).get();

        List<String> newTagNames = Arrays.asList("tag1", "tag2", "tag3","우정여행");
        List<String> newImageUrls = Arrays.asList("url1", "url2", "url3", "https://2");

        // 메서드 호출
        postService.updateTagsAndImages(post, newTagNames, newImageUrls);

        // 결과 확인
        List<Tag> updatedTags = tagRepository.findAllByPost(post);
        List<Image> updatedImages = imageRepository.findAllByPost(post);
        // updatedTags 출력
        System.out.println("Updated Tags:");
        updatedTags.forEach(tag -> System.out.println(tag.getName()));

        // updatedImages 출력
        System.out.println("Updated Images:");
        updatedImages.forEach(image -> System.out.println(image.getImgUrl()));

    }

    //@Test
    public void memberPostTest(){
        Post post = postRepository.findById(114L).get();
        Member member = post.getMember();
        System.out.println("Email:"+member.getEmail());
        System.out.println("Idx:"+member.getIdx());
        //System.out.println("Name:"+member.getName());
    }

    //@Test
    public void testGetPosts() {

        Member member = memberRepository.findByEmail("summer@naver.com").get();
        List<String> newTagNames = Arrays.asList("tag1", "tag2");
        List<String> newImageUrls = Arrays.asList("url1", "url2");

        PostRequestDto.UploadPostRequestDto uploadPostRequestDto = PostRequestDto.UploadPostRequestDto.builder()
                .title("test")
                .postType(PostType.POST)
                .body("양방향 test게시물입니다.")
                .location("24.12342,12.12344")
                .images(newImageUrls)
                .tags(newTagNames)
                .memberId(member.getMemberId())
                .build();
    postService.uploadPost(uploadPostRequestDto);
        List<Post> posts = member.getPosts();
        for (Post post : posts) {
            System.out.println("title : " + post.getTitle());
            System.out.println("body : " + post.getBody());
        }

    }


}
