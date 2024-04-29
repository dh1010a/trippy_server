package com.example.server.domain.blog.controller;

import com.example.server.domain.blog.dto.BlogRequestDto;
import com.example.server.domain.blog.dto.BlogResponseDto;
import com.example.server.domain.blog.dto.BlogResponseDto.IsDuplicatedDto;
import com.example.server.domain.blog.service.BlogService;
import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/blog")
public class BlogController {

    public final BlogService blogService;

    @GetMapping("/isDuplicated")
    public ApiResponse<?> isDuplicated(@RequestParam(value = "blogName") String blogName) {
        IsDuplicatedDto isDuplicatedDto;

        if (blogName != null) {
            isDuplicatedDto = BlogResponseDto.IsDuplicatedDto.builder()
                    .isDuplicated(blogService.isExistByBlogName(blogName))
                    .message(blogService.isExistByBlogName(blogName)? ErrorStatus.BLOG_NAME_ALREADY_EXIST.getMessage()
                            : "사용 가능한 블로그명입니다.")
                    .build();
        }

        else {
            throw new ErrorHandler(ErrorStatus._BAD_REQUEST);
        }
        return ApiResponse.onSuccess(isDuplicatedDto);
    }

    @PostMapping()
    public ApiResponse<?> createBlog(@RequestBody BlogRequestDto.CreateBlogRequestDto createBlogRequestDto) {
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return ApiResponse.onSuccess(blogService.createBlog(memberId, createBlogRequestDto));
    }
}
