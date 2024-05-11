package com.example.server.domain.post.controller;

import com.example.server.domain.member.controller.MemberController;
import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.service.PostService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @PostMapping("")
    public ApiResponse<?> uploadPost(@RequestBody PostRequestDto.UploadPostRequestDto uploadPostRequestDto) {
        String memberId = getLoginMemberId();
        uploadPostRequestDto.setMemberId(memberId);
        return ApiResponse.onSuccess(postService.uploadPost(uploadPostRequestDto));
    }

    @PatchMapping("")
    public  ApiResponse<?> updatePost(@RequestBody PostRequestDto.UpdatePostRequestDto updatePostRequestDto) {
        String memberId = getLoginMemberId();
        updatePostRequestDto.setMemberId(memberId);
        return ApiResponse.onSuccess(postService.updatePost(updatePostRequestDto));
    }


    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePost(@PathVariable("id") Long postId) {
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(postService.deletePost(postId,memberId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getPost(@PathVariable("id") Long postId) {
        return ApiResponse.onSuccess(postService.getPost(postId));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }



}
