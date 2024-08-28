package com.example.server.domain.bookmark.service;


import com.example.server.domain.bookmark.domain.BookMark;
import com.example.server.domain.bookmark.dto.BookMarkDtoConverter;
import com.example.server.domain.bookmark.dto.BookMarkResponseDto;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.BookMarkRepository;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.service.PostService;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    private final PostService postService;

    public BookMarkResponseDto.BookMarkBasicResponse postBookMark(Long postId, String memberId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        if (isBookMarked(memberId, postId)) {
            throw new ErrorHandler(ErrorStatus.BOOKMARK_ALREADY_EXIST);
        }

        BookMark bookMark = BookMark.builder()
                .post(post)
                .member(member)
                .build();
        bookMarkRepository.save(bookMark);
        return BookMarkDtoConverter.convertToBookMarkDto(bookMark);
    }

    public List<PostResponseDto.GetPostResponseDto> getBookMarkPostList(String memberId, String type, Integer page, Integer size, OrderType orderType) {
        Member member = getMemberById(memberId);
        Pageable pageable = postService.getPageable(page, size, orderType);
//        List<Long> bookMarkIds = bookMarkRepository.findAllByMember(member);

        List<Post> posts = bookMarkRepository.findBookMarkedPostsByMemberWithPostTypeAndScope(memberId, PostType.fromName(type) , pageable).getContent();
        return PostDtoConverter.convertToPostListResponseDto(posts, member);
    }

    public List<PostResponseDto.GetOotdPostResponseDto> getBookMarkOotdList(String memberId, String type, Integer page, Integer size, OrderType orderType) {
        Member member = getMemberById(memberId);
        Pageable pageable = postService.getPageable(page, size, orderType);
//        List<Long> bookMarkIds = bookMarkRepository.findAllByMember(member);

        List<Post> posts = bookMarkRepository.findBookMarkedPostsByMemberWithPostTypeAndScope(memberId, PostType.fromName(type) , pageable).getContent();
        return PostDtoConverter.convertToOOTDListResponseDto(posts, member);
    }

    public BookMarkResponseDto.BookMarkCountResponse getMyBookMarkCount(String memberId) {
        Member member = getMemberById(memberId);
        long postCnt = bookMarkRepository.countBookMarkedPostsByMemberWithPostTypeAndScope(memberId, PostType.POST);
        long ootdCnt = bookMarkRepository.countBookMarkedPostsByMemberWithPostTypeAndScope(memberId, PostType.OOTD);
        return BookMarkResponseDto.BookMarkCountResponse.builder()
                .postCount(postCnt)
                .ootdCount(ootdCnt)
                .totalCount(postCnt + ootdCnt)
                .build();
    }

    public boolean isBookMarked(String memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        return bookMarkRepository.existsByMemberAndPost(member, post);
    }

    public boolean deleteBookMark(String memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        BookMark bookMark = bookMarkRepository.findByMemberAndPost(member, post)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BOOKMARK_NOT_FOUND));
        if (isBookMarked(memberId, postId)) {
            bookMarkRepository.delete(bookMark);
            return true;
        }
        throw new ErrorHandler(ErrorStatus.BOOKMARK_NOT_EXIST);
    }


    public Member getMemberById(String memberId) {
        if ("anonymousUser".equals(memberId)) {
            return null;
        }
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
