package com.example.server.domain.recommend.controller;

import com.example.server.domain.member.model.InterestedType;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.recommend.dto.RecommendRequestDto;
import com.example.server.domain.recommend.service.RecommendService;
import com.example.server.domain.search.model.SearchType;
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
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;



//    @GetMapping("/post")
//    public ApiResponse<?> getRecommendPost(@RequestParam PostType postType) {
//        String memberId = getLoginMemberId();
//        log.info("추천 게시물 요청 : memberId = {}", memberId);
//        if(postType.equals(PostType.OOTD)) {
//            return ApiResponse.onSuccess(recommendService.getRecommendOotds(memberId, postType));
//        }
//        else {
//            return ApiResponse.onSuccess(recommendService.getRecommendPosts(memberId, postType));
//        }
//    }

    @GetMapping("/search")
    public ApiResponse<?> getRecommendSearch() {
        String memberId = getLoginMemberId();
        log.info("추천 검색어 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(recommendService.getRecommendSearch(memberId));
    }

    @GetMapping("/interest")
    public ApiResponse<?> getRecommendInterest(
            @RequestParam String interestedType,
            @RequestParam PostType postType
    ) {
        String memberId = getLoginMemberId();
        log.info("추천 게시물 요청 (관심 분야) : memberId = {}, type = {}", memberId, interestedType);
        if(postType.equals(PostType.OOTD)) {
            return ApiResponse.onSuccess(recommendService.getRecommendOotds(interestedType, postType, memberId));
        }
        else if (postType.equals(PostType.POST)) {
            return ApiResponse.onSuccess(recommendService.getRecommendPosts(interestedType, postType, memberId));
        }
        else {
            throw new ErrorHandler(ErrorStatus.POST_TYPE_ERROR);
        }
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    // 관광지 추천
    @GetMapping("/spot")
    public ApiResponse<?> getRecommendSpot(
            @RequestParam String area
    ) {
        String memberId = getLoginMemberId();
        log.info("추천 관광지 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(recommendService.getRecommendSpot(area));
    }
}
