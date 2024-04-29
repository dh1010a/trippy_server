package com.example.server.domain.post.service;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberDtoConverter;
import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.domain.member.model.ActiveState;
import com.example.server.domain.member.model.Gender;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.oauth2.model.SocialType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;


    public PostResponseDto.UploadPostResultResponseDto signUp(PostRequestDto.UploadPostRequestDto requestDto) {
        Member member = getMember(requestDto.getEmail());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);
        List<Tag> tags = saveTag(requestDto);
        Post post = savePost(requestDto);

        return PostDtoConverter.convertToMemberTaskDto(post);
        // tag  저장
        // image  저장

    }

    public Member getMember(String email) {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        Member member = null;
        if (memberOptional.isPresent()) {
            member = memberOptional.get();
        }
        return member;
    }

    // POST 저장 메서드
    public Post savePost(PostRequestDto.UploadPostRequestDto requestDto){
        Member member = getMember(requestDto.getEmail());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);

        Post post = Post.builder()
                .member((member))
                .title(requestDto.getTitle())
                .body(requestDto.getBody())
                .postType(requestDto.getPostType())
                .location(requestDto.getLocation())
                .build();
        return post;
    }

    public List<Tag> saveTag(PostRequestDto.UploadPostRequestDto requestDto){
        Member member = getMember(requestDto.getEmail());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);
        List<String> tagNameList = requestDto.getTags();
        List<Tag> tagList = new ArrayList<>();
        for (String name : tagNameList) {
            Tag tag = Tag.builder()
                            .name(name).build();
            tagList.add(tag);
        }
        return tagList;

    }

//    public void savePostWithTagsAndImage(Post post, List<Tag> tags, List<Image> images) {
//        // Post 엔티티와 연관된 Tag 엔티티에 Post 엔티티를 설정
//        for (Tag tag : tags) {
//            tag.setPost(post);
//        }
//
//
//        // 부모 엔티티와 자식 엔티티를 함께 저장
//        post.setTags(tags);
//        postRepository.save(post);
//    }


}
