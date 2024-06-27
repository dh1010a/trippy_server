package com.example.server.domain.post.controller;

import com.example.server.domain.post.dto.OotdReqResDto;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.service.OotdService;
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
@RequestMapping("/api/ootd")
public class OotdController {

    private final OotdService ootdService;

    @PostMapping("")
    public ApiResponse<?> uploadOotdPost(@RequestBody PostRequestDto.UploadOOTDPostRequestDto requestDto) {
        String memberId = getLoginMemberId();
        requestDto.getPostRequest().setMemberId(memberId);
        log.info("OOTD 게시물 업로드 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(ootdService.uploadOotdPost(requestDto));
    }

    @PatchMapping("")
    public  ApiResponse<?> updateOotd(@RequestBody OotdReqResDto.UpdateOOTDRequestDto updateOOTDRequestDto) {
        String memberId = getLoginMemberId();
        log.info("OOTD 게시물 업데이트 요청 : memberId = {}, ticketId = {}", memberId, updateOOTDRequestDto.getId() );
        return ApiResponse.onSuccess(ootdService.updateOotd(memberId, updateOOTDRequestDto));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getPost(@PathVariable("id") Long postId) {
        log.info("OOTD 게시물 조회 요청 : postId = {}",postId );
        return ApiResponse.onSuccess(ootdService.getPost(postId));
    }

    @GetMapping("/all")
    public ApiResponse<?> getAllPost(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if(page == null) page = 0;
        if(size==null) size = 0;
        log.info("모든 OOTD 게시물 조회 요청 ");
        return ApiResponse.onSuccess(ootdService.getAllPost(page, size));
    }

    @GetMapping()
    public ApiResponse<?> getAllMemberPost(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if(page == null) page = 0;
        if(size==null) size = 0;
        String memberId = getLoginMemberId();
        log.info("회원별 OOTD 게시물 조회 요청 : memberId = {}", memberId );
        return ApiResponse.onSuccess(ootdService.getAllMemberPost(memberId,page,size));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
