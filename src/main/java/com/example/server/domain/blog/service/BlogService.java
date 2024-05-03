package com.example.server.domain.blog.service;

import com.example.server.domain.blog.domain.Blog;
import com.example.server.domain.blog.dto.BlogDtoConverter;
import com.example.server.domain.blog.dto.BlogRequestDto;
import com.example.server.domain.blog.dto.BlogResponseDto;
import com.example.server.domain.blog.dto.BlogResponseDto.CreateBlogResponseDto;
import com.example.server.domain.blog.repository.BlogRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.server.domain.blog.dto.BlogRequestDto.*;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final MemberRepository memberRepository;

    public CreateBlogResponseDto createBlog(String memberId, CreateBlogRequestDto createBlogRequestDto) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.updateNickName(createBlogRequestDto.getNickName());

        if (isExistByBlogName(createBlogRequestDto.getName())) {
            throw new ErrorHandler(ErrorStatus.BLOG_NAME_ALREADY_EXIST);
        }

        Blog blog = Blog.builder()
                .name(createBlogRequestDto.getName())
                .introduce(createBlogRequestDto.getIntroduce())
                .build();

        blogRepository.save(blog);

        member.setBlog(blog);

        return BlogDtoConverter.convertToBlogResponseDto(blog);
    }

    public boolean isExistByBlogName(String name) {
        return blogRepository.existsByName(name);
    }

}
