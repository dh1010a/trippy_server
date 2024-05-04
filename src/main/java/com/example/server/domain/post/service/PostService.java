package com.example.server.domain.post.service;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.repository.ImageRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.repository.LikeRepository;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.server.domain.image.dto.ImageDtoConverter.convertToImage;
import static com.example.server.domain.post.dto.TagDtoConverter.convertToTag;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;

    // POST api/post/
    public PostResponseDto.PostBasicResponseDto uploadPost(PostRequestDto.UploadPostRequestDto requestDto) {
        Member member = getMember(requestDto.getMemberId());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);
        Post post = savePost(requestDto);
        List<Tag> tags = saveTags(requestDto, post);
        List<Image> images = saveImages(requestDto,post);
        savePostWithTagsAndImage(post, tags, images);
        return PostDtoConverter.convertToPostBasicDto(post);
    }

    // GET api/post
    public PostResponseDto.GetPostResponseDto getPost(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        return PostDtoConverter.convertToGetResponseDto(post);
    }

    // DELETE api/post
    public PostResponseDto.DeletePostResultResponseDto deletePost(Long postId, String memberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        if(!((post.getMember().getEmail()).equals(memberId))) throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
        postRepository.delete(post);
        return PostDtoConverter.convertToDeletePostDto(postId);
    }

    // PATCH api/post
    public PostResponseDto.PostBasicResponseDto updatePost(PostRequestDto.UpdatePostRequestDto requestDto) {
        Post post = postRepository.findById(requestDto.getId()).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        Member member =  getMember(requestDto.getMemberId());
        if(!((post.getMember().getIdx()).equals(member.getIdx()))) throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
        updateTagsAndImages(post,requestDto.getTags(),requestDto.getImages());
        post.updatePost(requestDto);

        return PostDtoConverter.convertToPostBasicDto(post);
    }

    // GET member 메서드
    public Member getMember(String email) {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        Member member = null;
        if (memberOptional.isPresent()) {
            member = memberOptional.get();
        }
        return member;
    }

    // POST 빌드 메서드
    public Post savePost(PostRequestDto.UploadPostRequestDto requestDto){
        Member member = getMember(requestDto.getMemberId());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);

        Post post = Post.builder()
                .member(member)
                .title(requestDto.getTitle())
                .body(requestDto.getBody())
                .postType(requestDto.getPostType())
                .location(requestDto.getLocation())
                .build();
        return post;
    }

    // TAG 엔티티 저장 메서드
    public List<Tag> saveTags(PostRequestDto.UploadPostRequestDto requestDto,Post post) {
        return requestDto.getTags().stream()
                .map(tagName -> {

                    Tag tag = Tag.builder().name(tagName)
                            .post(post)
                            .build();
                    tagRepository.save(tag);
                    return tag;
                })
                .collect(Collectors.toList());
    }

    // IMAGE 저장 메서드 for [POST api/post/]
    public List<Image> saveImages(PostRequestDto.UploadPostRequestDto requestDto,Post post) {
        return requestDto.getImages().stream()
                .map(imageUrl -> {
                    Image image = Image.builder().imgUrl(imageUrl)
                            .post(post)
                            .build();
                    imageRepository.save(image);
                    return image;
                })
                .collect(Collectors.toList());
    }

    // POST 저장 메서드 for [POST api/post/]
    public void savePostWithTagsAndImage(Post post, List<Tag> tags, List<Image> images) {
        post.updateTags(tags);
        post.updateImages(images);
        postRepository.save(post);
    }

    public void updateTagsAndImages(Post post,List<String> newTagNames, List<String> newImageUrls) {
        // 기존 태그 및 이미지 가져오기
        List<Tag> originalTags = tagRepository.findAllByPost(post);
        List<Image> originalImages = imageRepository.findAllByPost(post);

        // 새로운 태그 추가
        for (String newTagName : newTagNames) {
            // 만약 기존 TAG에 현재 Tag name 없으면 Tag 새로 추가
            if (originalTags.stream().noneMatch(tag -> tag.getName().equals(newTagName))) {
                Tag newTag = Tag.builder()
                        .name(newTagName)
                        .post(post)
                        .build();
                tagRepository.save(newTag);
            }
            // 기존 TAG에 현재 Tag name 있으면 -> 변동 없다는 뜻
            else {
                originalTags.removeIf(tag -> tag.getName().equals(newTagName));
            }
        }
        // 새로운 이미지 추가
        for (String newImageUrl : newImageUrls) {
            if (originalImages.stream().noneMatch(image -> image.getImgUrl().equals(newImageUrl))) {
                Image newImage = Image.builder()
                                .imgUrl(newImageUrl)
                                .post(post)
                                .build();
                imageRepository.save(newImage);
            }
            else {
                originalImages.removeIf(image -> image.getImgUrl().equals(newImageUrl));
            }
        }
        for (Tag tag : originalTags) {
            tagRepository.delete(tag);
        }

        // 기존 이미지 삭제
        for (Image image : originalImages) {
            imageRepository.delete(image);
        }
    }




}
