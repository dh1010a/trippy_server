package com.example.server.domain.post.controller;

import com.example.server.domain.post.dto.LikeResponseDto;
import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
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
        log.info("게시물 좋아요 요청 : memberId = {}, postId = {}", memberId ,postId);
        return ApiResponse.onSuccess(likeService.likeToPost(postId,memberId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?>  PostLikeList(@PathVariable("id") Long postId){
        String memberId = getLoginMemberId();
        log.info("게시물 좋아요 리스트 요청 : memberId = {}, postId = {}", memberId ,postId);
        return ApiResponse.onSuccess(likeService.PostLikeList(postId));
    }

    @GetMapping("/isLiked/{id}")
    public ApiResponse<?>  isLiked(@PathVariable("id") Long postId){
        String memberId = getLoginMemberId();

        return ApiResponse.onSuccess(likeService.isLiked(postId,memberId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?>  deletePostLike(@PathVariable("id") Long postId){
        String memberId = getLoginMemberId();
        log.info("게시물 좋아요 취소 요청 : memberId = {}, postId = {}", memberId ,postId);
        return ApiResponse.onSuccess(likeService.deletePostLike(postId,memberId));
    }

    @GetMapping("/my-list")
    public ApiResponse<?>  getMyLikePost(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "0") Integer size,
                                         @RequestParam PostType postType){
        String memberId = getLoginMemberId();
        log.info("내 좋아요 게시물 조회 : memberId = {}", memberId);
        if(postType.equals(PostType.POST)) {
            return ApiResponse.onSuccess(likeService.getLikePosts(memberId, postType, page, size));
        } else {
            return ApiResponse.onSuccess(likeService.getLikeOotds(memberId, postType, page, size));
        }
    }

    @GetMapping("/count/my")
    public ApiResponse<?> getTotalCountByLoginMember(@RequestParam PostType type) {
        String memberId = getLoginMemberId();
        log.info("{}의 {} 좋아요 게시물 개수 출력", memberId, type);
        return ApiResponse.onSuccess(likeService.getLikePostCount(memberId, type));
    }


    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
