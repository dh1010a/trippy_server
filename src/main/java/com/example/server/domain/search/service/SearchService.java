package com.example.server.domain.search.service;

import com.example.server.domain.follow.repository.MemberFollowRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.domain.post.service.PostService;
import com.example.server.domain.search.dto.SearchRequestDto;
import com.example.server.domain.search.dto.SearchResponseDto;
import com.example.server.domain.search.model.SearchType;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.server.domain.search.dto.SearchDtoConverter.convertToSearchMemberDto;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final SearchRedisService searchRedisService;
    private final MemberFollowRepository memberFollowRepository;
    private final PostService postService;
    private final TagRepository tagRepository;

    public List<PostResponseDto.GetPostResponseDto> getPosts(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        updateSearchLog(memberId, saveSearchRequest);
        Member member = !"anonymousUser".equals(memberId) ? getMember(memberId) : null;
        List<Long> followingList = memberFollowRepository.findFollowingList(member==null ? 0 : member.getIdx());

        // Page<Post> 객체를 통해 전체 데이터를 포함한 정보를 가져옴
//        Page<Post> postPage = postRepository.findPostByTitle(saveSearchRequest.getKeyword(), PostType.OOTD, followingList, pageable);
//
//        // 전체 갯수는 postPage.getTotalElements()로 가져옴
//        long totalPosts = postPage.getTotalElements();  // 전체 게시물 수
//
//        // 페이지별 게시물 목록
//        List<Post> posts = postPage.getContent();

        Pageable pageable = postService.getPageable(saveSearchRequest.getPage(), saveSearchRequest.getSize(), saveSearchRequest.getOrderType());

        List<Post> posts = postRepository.findPostByTitle(saveSearchRequest.getKeyword(), PostType.POST, followingList, pageable).getContent();
        return PostDtoConverter.convertToPostListResponseDto(posts, member);
    }

    public List<PostResponseDto.GetOotdPostResponseDto> getOotds(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        updateSearchLog(memberId, saveSearchRequest);
        Member member = !"anonymousUser".equals(memberId) ? getMember(memberId) : null;
        List<Long> followingList = memberFollowRepository.findFollowingList(member==null ? 0 : member.getIdx());
        Pageable pageable = postService.getPageable(saveSearchRequest.getPage(), saveSearchRequest.getSize(), saveSearchRequest.getOrderType());

        List<Post> posts = postRepository.findPostBodyAndTitle(saveSearchRequest.getKeyword(), PostType.OOTD, followingList, pageable).getContent();
        return PostDtoConverter.convertToOOTDListResponseDto(posts, member);
    }

    public List<SearchResponseDto.SearchMemberDto> getMembers(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        List<Member> members;

        Pageable pageable = getPageable(saveSearchRequest.getPage(), saveSearchRequest.getSize(), saveSearchRequest.getOrderType());
        if(saveSearchRequest.getSearchType().equals(SearchType.NICKNAME)) {
            members = memberRepository.findByNicknameContaining(saveSearchRequest.getKeyword(), pageable).getContent();
        }
        else{
            members = memberRepository.findByBlogNameContaining(saveSearchRequest.getKeyword(), pageable).getContent();
        }
        return convertToSearchMemberDto(members);
    }

    private Pageable getPageable(Integer page, Integer size, OrderType orderType) {
        if (page == 0 && size == 0) {
            return Pageable.unpaged();
        } else {
            return PageRequest.of(page, size, getSortByOrderType(orderType));
        }
    }

    private Sort getSortByOrderType(OrderType orderType) {
        switch (orderType) {
            case LIKE:
                return Sort.by(Sort.Direction.DESC, "followerCnt");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
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

    public List<PostResponseDto.GetPostResponseDto> getPostSearchByTag(String tag, String memberId, Integer size, Integer page) {
        return getPostsByTag(tag, memberId, size, page, PostType.POST);
    }

    public List<PostResponseDto.GetOotdPostResponseDto> getOotdSearchByTag(String tag, String memberId, Integer size, Integer page) {
        return getPostsByTag(tag, memberId, size, page, PostType.OOTD);
    }

    private <T> List<T> getPostsByTag(String tag, String memberId, Integer size, Integer page, PostType postType) {
        Member member = getMember(memberId);
        List<Post> posts;

        if (size == 0 && page == 0) {
            posts = tagRepository.findPostsByTag(tag).stream()
                    .filter(post -> post.getPostType().equals(postType))
                    .collect(Collectors.toList());
        } else {
            Pageable pageable = PageRequest.of(page, size);
            posts = tagRepository.findPostsByTag(tag, pageable).getContent().stream()
                    .filter(post -> post.getPostType().equals(postType))
                    .collect(Collectors.toList());
        }

        if (postType.equals(PostType.POST)) {
            return (List<T>) PostDtoConverter.convertToPostListResponseDto(posts, member);
        } else {
            return (List<T>) PostDtoConverter.convertToOOTDListResponseDto(posts, member);
        }
    }


    private Member getMember(String memberId) {
        if ("anonymousUser".equals(memberId)) {
            return null;
        }
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}

