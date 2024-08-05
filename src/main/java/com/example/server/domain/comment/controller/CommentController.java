package com.example.server.domain.comment.controller;

import com.example.server.domain.comment.dto.CommentRequestDto;
import com.example.server.domain.comment.service.CommentService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
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
    public ApiResponse<?> uploadComment(@RequestBody CommentRequestDto.CommentBasicRequest commentBasicRequest,  HttpSession session) {
        String memberId = getLoginMemberId();
        commentBasicRequest.setMemberId(memberId);
        log.info("댓글 작성 요청 : postId = {}, memberId = {}",commentBasicRequest.getPostId(),  memberId);
        return ApiResponse.onSuccess(commentService.uploadComment(commentBasicRequest,session));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getCommentByCommentId(@PathVariable("id") Long commentId) {
        log.info("댓글 정보 조회 요청 : commentId = {}",commentId);
        return ApiResponse.onSuccess(commentService.getCommentById(commentId));
    }

    @GetMapping("")
    public ApiResponse<?> getCommentByPostId(@RequestParam("postId") Long postId) {
        log.info("게시물의 댓글 정보 조회 요청 : postId = {}",postId);
        return ApiResponse.onSuccess(commentService.getCommentTree(postId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteComment(@PathVariable("id") Long commentId) {
        String memberId = getLoginMemberId();
        log.info("댓글 삭제 요청: commentId = {}, memberId= {}",commentId,memberId);
        return ApiResponse.onSuccess(commentService.deleteComment(memberId, commentId));
    }

    @PatchMapping("")
    public ApiResponse<?> updateComment(@RequestBody CommentRequestDto.CommentUpdateRequest requestDto){
        String memberId = getLoginMemberId();
        requestDto.setMemberId(memberId);
        log.info("댓글 내용 업데이트 요청: commentId = {}, memberId= {}",requestDto.getCommentId(),memberId);
        return ApiResponse.onSuccess(commentService.updateComment(requestDto));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
