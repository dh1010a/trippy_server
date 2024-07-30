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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SearchRedisService searchRedisService;

    public List<PostResponseDto.GetPostResponseDto> getPosts(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        updateSearchLog(memberId, saveSearchRequest);
        Member member = !"anonymousUser".equals(memberId) ? getMember(memberId) : null;
        List<Post> posts = retrievePosts(saveSearchRequest);
        return PostDtoConverter.convertToPostListResponseDto(posts, member);
    }

    public List<PostResponseDto.GetOotdPostResponseDto> getOotds(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        updateSearchLog(memberId, saveSearchRequest);
        Member member = !"anonymousUser".equals(memberId) ? getMember(memberId) : null;
        List<Post> posts = retrievePosts(saveSearchRequest);
        return PostDtoConverter.convertToOOTDListResponseDto(posts, member);
    }

    private void updateSearchLog(String memberId, SearchRequestDto.SaveSearchRequest saveSearchRequest){
        // 전체 검색어 count
        searchRedisService.incrementCount("popularSearches" + saveSearchRequest.getPostType(), saveSearchRequest.getKeyword());
        if (!"anonymousUser".equals(memberId)) {
            // 회원별 검색어 log
            searchRedisService.saveRecentSearch(memberId, saveSearchRequest.getKeyword(), saveSearchRequest.getPostType());
            // 회원별 검색어 count
            searchRedisService.incrementCount("member:" + memberId + ":popularSearches" + saveSearchRequest.getPostType(), saveSearchRequest.getKeyword());
        }
    }

    private List<Post> retrievePosts(SearchRequestDto.SaveSearchRequest saveSearchRequest) {
        if (saveSearchRequest.getPage() == 0 && saveSearchRequest.getSize() == 0) {
            return getPostsWithoutPagination(saveSearchRequest);
        } else {
            Pageable pageable = PageRequest.of(saveSearchRequest.getPage(), saveSearchRequest.getSize());
            return getPostsWithPagination(saveSearchRequest, pageable);
        }
    }

    private List<Post> getPostsWithoutPagination(SearchRequestDto.SaveSearchRequest saveSearchRequest) {
        return switch (saveSearchRequest.getSearchType()) {
            case "title" -> postRepository.findPostByTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType());
            case "titleAndContent" -> postRepository.findPostBodyAndTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType());
            default -> Collections.emptyList();
        };
    }

    private List<Post> getPostsWithPagination(SearchRequestDto.SaveSearchRequest saveSearchRequest, Pageable pageable) {
        Page<Post> resultPage = switch (saveSearchRequest.getSearchType()) {
            case "title" -> postRepository.findPostByTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType(), pageable);
            case "titleAndContent" -> postRepository.findPostBodyAndTitle(saveSearchRequest.getKeyword(), saveSearchRequest.getPostType(), pageable);
            default -> Page.empty(pageable);
        };
        return resultPage.getContent();
    }

    private Member getMember(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
