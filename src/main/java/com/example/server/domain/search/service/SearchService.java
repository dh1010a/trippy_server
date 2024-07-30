package com.example.server.domain.search.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.search.dto.SearchRequestDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final RedisUtil redisUtil;

    public List<PostResponseDto.GetPostResponseDto> getPosts(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        // 검색어 저장
        if(!memberId.equals("anonymousUser")){
            saveRecentSearch(memberId,saveSearchRequest.getKeyword(), saveSearchRequest.getPostType());
        }
        Member member = !memberId.equals("anonymousUser") ? Optional.ofNullable(getMember(memberId)).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND)) : null;
        List<Post> posts;
        if (saveSearchRequest.getPage() == 0 && saveSearchRequest.getSize() == 0) {
            posts = getPostsWithoutPagination(saveSearchRequest);
        } else {
            Pageable pageable = PageRequest.of(saveSearchRequest.getPage(), saveSearchRequest.getSize());
            posts = getPostsWithPagination(saveSearchRequest, pageable);
        }
        return PostDtoConverter.convertToPostListResponseDto(posts,member);
    }


    public List<PostResponseDto.GetOotdPostResponseDto> getOotds(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId){
        // 검색어 저장
        if(!memberId.equals("anonymousUser")){
            saveRecentSearch(memberId,saveSearchRequest.getKeyword(), saveSearchRequest.getPostType());
        }
        Member member = !memberId.equals("anonymousUser") ? Optional.ofNullable(getMember(memberId)).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND)) : null;
        List<Post> posts;
        if (saveSearchRequest.getPage() == 0 && saveSearchRequest.getSize() == 0) {
            posts = getPostsWithoutPagination(saveSearchRequest);
        } else {
            Pageable pageable = PageRequest.of(saveSearchRequest.getPage(), saveSearchRequest.getSize());
            posts = getPostsWithPagination(saveSearchRequest, pageable);
        }
        return PostDtoConverter.convertToOOTDListResponseDto(posts,member);
    }

    // 페이지네이션이 없는 경우
    private List<Post> getPostsWithoutPagination(SearchRequestDto.SaveSearchRequest saveSearchRequest) {
        if ("title".equals(saveSearchRequest.getSearchType())) {
            return postRepository.findPostByTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType());
        } else if ("titleAndContent".equals(saveSearchRequest.getSearchType())) {
            return postRepository.findPostBodyAndTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType());
        }
        return Collections.emptyList();
    }

    // 페이지네이션이 있는 경우
    private List<Post> getPostsWithPagination(SearchRequestDto.SaveSearchRequest saveSearchRequest, Pageable pageable) {
        Page<Post> resultPage;
        if ("title".equals(saveSearchRequest.getSearchType())) {
            resultPage = postRepository.findPostByTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType(), pageable);
        } else if ("titleAndContent".equals(saveSearchRequest.getSearchType())) {
            resultPage = postRepository.findPostBodyAndTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType(), pageable);
        } else {
            return Collections.emptyList();
        }

        return resultPage.getContent();
    }

    public void saveRecentSearch(String memberId, String keyword, PostType postType){
        String key = "SearchLog" + postType + memberId;

        if(redisUtil.getSize(key) == 10) {
            redisUtil.deleteOldData(key);
        }
        redisUtil.pushSearchLog(key,keyword);

    }

    public List<String> getRecentSearch(String memberId, PostType postType){
        String key = "SearchLog" + postType + memberId;
        return redisUtil.getAllData(key);
    }

    public Member getMember(String memberId) {
        Optional<Member> memberOptional = memberRepository.findByMemberId(memberId);
        Member member = null;
        if (memberOptional.isPresent()) {
            member = memberOptional.get();
        }
        return member;
    }
}
