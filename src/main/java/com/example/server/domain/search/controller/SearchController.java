package com.example.server.domain.search.controller;

import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.search.dto.SearchRequestDto;
import com.example.server.domain.search.model.SearchType;
import com.example.server.domain.search.service.SearchRedisService;
import com.example.server.domain.search.service.SearchService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import jakarta.ws.rs.GET;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;
    private final SearchRedisService searchRedisService;

    @GetMapping ("")
    public ApiResponse<?> getSearchPostList(
            @RequestParam String keyword,
            @RequestParam PostType postType,
            @RequestParam SearchType searchType,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "0") Integer size

    ) {
        String memberId = getLoginMemberId();
        SearchRequestDto.SaveSearchRequest saveSearchRequest = SearchRequestDto.SaveSearchRequest.builder()
                .keyword(keyword)
                .postType(postType)
                .searchType(searchType)
                .page(page)
                .size(size)
                .build();
        log.info("게시물 검색 요청 : memberId = {}, keyword = {}", memberId ,saveSearchRequest.getKeyword());
        if (saveSearchRequest.getPostType().equals(PostType.POST)) {
            return ApiResponse.onSuccess(searchService.getPosts(saveSearchRequest, memberId));
        } else {
            return ApiResponse.onSuccess(searchService.getOotds(saveSearchRequest, memberId));
        }
    }

    @GetMapping("/recent")
    public ApiResponse<?> getCurrentSearchLog(@RequestParam  PostType postType){
        String memberId = getLoginMemberId();
        log.info("최근 검색어 조회 요청 : memberId = {}", memberId);
        List<String> searchLogs;
        if(memberId.equals("anonymousUser")) {
            searchLogs = Collections.emptyList();
        }
        else {
            String key = "SearchLog" + postType + memberId;
            searchLogs = searchRedisService.getRecentSearch(key);
        }
        return ApiResponse.onSuccess(searchLogs);
    }

    @GetMapping("/popular")
    public ApiResponse<?> getPopularSearch(@RequestParam  PostType postType) {
        String memberId = getLoginMemberId();
        log.info("인기 검색어 조회 요청 : memberId = {}", memberId);
        String key = "popularSearches" + postType;
        List<String> searchLogs = searchRedisService.getDESCList(key);
        return ApiResponse.onSuccess(searchLogs);

    }


    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }


}
