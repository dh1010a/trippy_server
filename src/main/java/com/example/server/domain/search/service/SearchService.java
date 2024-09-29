package com.example.server.domain.search.service;

import com.example.server.domain.follow.repository.MemberFollowRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Scope;
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

    public PostResponseDto.GetMultiplePostResponseDto getPosts(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        updateSearchLog(memberId, saveSearchRequest);
        Member member = !"anonymousUser".equals(memberId) ? getMember(memberId) : null;
        List<Long> followingList = memberFollowRepository.findFollowingList(member==null ? 0 : member.getIdx());

        Pageable pageable = postService.getPageable(saveSearchRequest.getPage(), saveSearchRequest.getSize(), saveSearchRequest.getOrderType());

        List<Post> posts = postRepository.findPostBodyAndTitleAndTag(saveSearchRequest.getKeyword(), PostType.POST, followingList, pageable).getContent();
        return PostDtoConverter.convertToMultiplePostResponseDto(posts, member);
    }

    public PostResponseDto.GetMultipleOotdResponseDto getOotds(SearchRequestDto.SaveSearchRequest saveSearchRequest, String memberId) {
        updateSearchLog(memberId, saveSearchRequest);
        Member member = !"anonymousUser".equals(memberId) ? getMember(memberId) : null;
        List<Long> followingList = memberFollowRepository.findFollowingList(member==null ? 0 : member.getIdx());
        Pageable pageable = postService.getPageable(saveSearchRequest.getPage(), saveSearchRequest.getSize(), saveSearchRequest.getOrderType());

        List<Post> posts = postRepository.findPostBodyAndTitleAndTag(saveSearchRequest.getKeyword(), PostType.OOTD, followingList, pageable).getContent();
        return PostDtoConverter.convertToMultipleOotdResponseDto(posts, member);
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
        searchRedisService.incrementCount("popularSearches" + saveSearchRequest.getSearchType(), saveSearchRequest.getKeyword());
        if (!"anonymousUser".equals(memberId)) {
            // 회원별 검색어 log
            searchRedisService.saveRecentSearch(memberId, saveSearchRequest.getKeyword(), saveSearchRequest.getSearchType());
            // 회원별 검색어 count
            searchRedisService.incrementCount("member:" + memberId + ":popularSearches" + saveSearchRequest.getSearchType(), saveSearchRequest.getKeyword());
        }
    }

    public PostResponseDto.GetMultiplePostResponseDto getPostSearchByTag(String tag, String memberId, Integer size, Integer page) {
        return (PostResponseDto.GetMultiplePostResponseDto) getPostsByTag(tag, memberId, size, page, PostType.POST);
    }

    public PostResponseDto.GetMultipleOotdResponseDto getOotdSearchByTag(String tag, String memberId, Integer size, Integer page) {
        return (PostResponseDto.GetMultipleOotdResponseDto) getPostsByTag(tag, memberId, size, page, PostType.OOTD);
    }

    private Object getPostsByTag(String tag, String memberId, Integer size, Integer page, PostType postType) {
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
        for (Post post : posts) {
            if (!isValidAccessToPost(post, member, postType)) {
                posts.remove(post);
            }
        }
        if (postType == PostType.POST) {
            return PostDtoConverter.convertToMultiplePostResponseDto(posts, member);
        } else {
            return PostDtoConverter.convertToMultipleOotdResponseDto(posts, member);
        }
    }

    private boolean isValidAccessToPost(Post post, Member member, PostType postType) {
        if (postType == PostType.OOTD) {
            if (post.getMember().getOotdScope() == Scope.PRIVATE) {
                return false;
            }
            if (post.getMember().getOotdScope() == Scope.PROTECTED) {
                if (member == null) {
                    return false;
                }
                return isFollower(member, post.getMember());
            }

        } else if (postType == PostType.POST) {
            if (post.getMember().getTicketScope() == Scope.PRIVATE) {
                return false;
            }
            if (post.getMember().getTicketScope() == Scope.PROTECTED) {
                if (member == null) {
                    return false;
                }
                return isFollower(member, post.getMember());
            }
        }
        return true;
    }

    private boolean isFollower(Member member, Member targetMember) {
        return memberFollowRepository.existsByMemberAndFollowingMemberIdx(member, targetMember.getIdx());
    }


    private Member getMember(String memberId) {
        if ("anonymousUser".equals(memberId)) {
            return null;
        }
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}

