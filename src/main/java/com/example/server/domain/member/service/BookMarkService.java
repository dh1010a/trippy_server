package com.example.server.domain.member.service;


import com.example.server.domain.member.domain.BookMark;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.BookMarkDtoConverter;
import com.example.server.domain.member.dto.BookMarkResponseDto;
import com.example.server.domain.member.repository.BookMarkRepository;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public BookMarkResponseDto.BookMarkBasicResponse postBookMark(Long postId, String memberId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        BookMark bookMark = BookMark.builder()
                .post(post)
                .member(member)
                .build();
        bookMarkRepository.save(bookMark);
        return BookMarkDtoConverter.convertToBookMarkDto(bookMark);
    }
}
