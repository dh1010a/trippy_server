package com.example.server.domain.post.controller;

import com.example.server.domain.post.dto.LikeResponseDto;
import com.example.server.domain.post.service.LikeService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{id}")
    public ApiResponse<?>  likeToPost(@PathVariable("id") Long postId){
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(likeService.likeToPost(postId,memberId));
    }

    @GetMapping("/isLiked/{id}")
    public ApiResponse<?>  isLiked(@PathVariable("id") Long postId){
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(likeService.isLiked(postId,memberId));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
