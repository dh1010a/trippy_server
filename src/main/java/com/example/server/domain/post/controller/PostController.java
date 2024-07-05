package com.example.server.domain.post.controller;

import com.example.server.domain.member.controller.MemberController;
import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.model.PostType;
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
        uploadPostRequestDto.getPostRequest().setMemberId(memberId);
        log.info("게시물 업로드 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(postService.uploadPost(uploadPostRequestDto));
    }

    @PatchMapping("")
    public  ApiResponse<?> updatePost(@RequestBody PostRequestDto.UpdatePostRequestDto updatePostRequestDto) {
        String memberId = getLoginMemberId();
        updatePostRequestDto.setMemberId(memberId);
        log.info("게시물 업데이트 요청 : memberId = {}, postId = {}", memberId,updatePostRequestDto.getId() );
        return ApiResponse.onSuccess(postService.updatePost(updatePostRequestDto));
    }


    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePost(@PathVariable("id") Long postId) {
        String memberId = getLoginMemberId();
        log.info("게시물 삭제 요청 : memberId = {}, postId = {}", memberId,postId );
        return ApiResponse.onSuccess(postService.deletePost(postId,memberId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getPost(@PathVariable("id") Long postId) {
        log.info("게시물 조회 요청 : postId = {}",postId );
        return ApiResponse.onSuccess(postService.getPost(postId));
    }

    @GetMapping("/all")
    public ApiResponse<?> getAllPost(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if(page == null) page = 0;
        if(size==null) size = 0;
        log.info("모든 게시물 조회 요청 ");
         return ApiResponse.onSuccess(postService.getAllPost(page, size));
    }

    @GetMapping("/my")
    public ApiResponse<?> getAllLoginMemberPost(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if(page == null) page = 0;
        if(size==null) size = 0;
        String memberId = getLoginMemberId();
        log.info("회원별 게시물 조회 요청 : memberId = {}", memberId );
        return ApiResponse.onSuccess(postService.getAllMemberPost(memberId,page,size));
    }

    @GetMapping("/by-member")
    public ApiResponse<?> getAllMemberPost(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam String memberId
    ) {
        if(page == null) page = 0;
        if(size==null) size = 0;
        log.info("회원별 게시물 조회 요청 : memberId = {}", memberId );
        return ApiResponse.onSuccess(postService.getAllMemberPost(memberId,page,size));
    }

    @GetMapping("/all/count")
    public ApiResponse<?> getTotalCount(@RequestParam PostType type) {
        log.info("{} 전체 개수 출력", type );
        return ApiResponse.onSuccess(postService.getTotalCount(type));
    }
    // 로그인 된 멤버
    @GetMapping("/count/my")
    public ApiResponse<?> getTotalCountByLoginMember(@RequestParam PostType type) {
        String memberId = getLoginMemberId();
        log.info("{}의 {} 개수 출력", memberId, type);
        return ApiResponse.onSuccess(postService.getTotalCountByMember(memberId,type));
    }

    @GetMapping("/count/by-member")
    public ApiResponse<?> getTotalCountByMember(
            @RequestParam PostType type,
            @RequestParam String memberId
    ) {
        log.info("{}의 {} 개수 출력", memberId, type);
        return ApiResponse.onSuccess(postService.getTotalCountByMember(memberId,type));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }



}
