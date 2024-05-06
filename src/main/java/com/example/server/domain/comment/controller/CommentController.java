package com.example.server.domain.comment.controller;

import com.example.server.domain.comment.dto.CommentRequestDto;
import com.example.server.domain.comment.service.CommentService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("")
    public ApiResponse<?> uploadComment(@RequestBody CommentRequestDto.CommentBasicRequest commentBasicRequest) {
        String memberId = getLoginMemberId();
        commentBasicRequest.setMemberId(memberId);
        return ApiResponse.onSuccess(commentService.uploadComment(commentBasicRequest));
    }

//    @GetMapping("")
//    public ApiResponse<?> getCommentF(@PathVariable("id") Long postId) {
//
//    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
