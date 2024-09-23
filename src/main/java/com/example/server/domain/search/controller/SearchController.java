package com.example.server.domain.search.controller;

import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.model.OrderType;
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
            @RequestParam SearchType searchType,
            @RequestParam(defaultValue = "LATEST") OrderType orderType,
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "0") Integer size

    ) {
        String memberId = getLoginMemberId();
        SearchRequestDto.SaveSearchRequest saveSearchRequest = SearchRequestDto.SaveSearchRequest.builder()
                .keyword(keyword)
                .postType(postType)
                .orderType(orderType)
                .searchType(searchType)
                .page(page)
                .size(size)
                .build();
        log.info("게시물 검색 요청 : memberId = {}, keyword = {}", memberId ,saveSearchRequest.getKeyword());
        // 게시물 반환
        if(searchType.equals(SearchType.POST)){
                return ApiResponse.onSuccess(searchService.getPosts(saveSearchRequest, memberId));
        }
        else if(searchType.equals(SearchType.OOTD)) {
            return ApiResponse.onSuccess(searchService.getOotds(saveSearchRequest, memberId));
        }
        // 닉네임, 블로그 ->  블로그 반환
        else {
            return ApiResponse.onSuccess(searchService.getMembers(saveSearchRequest, memberId));
        }

    }

    @GetMapping("/recent")
    public ApiResponse<?> getCurrentSearchLog(@RequestParam  SearchType searchType){
        String memberId = getLoginMemberId();
        log.info("최근 검색어 조회 요청 : memberId = {}", memberId);
        List<String> searchLogs;
        if(memberId.equals("anonymousUser")) {
            searchLogs = Collections.emptyList();
        }
        else {
            String key = "SearchLog" + searchType + memberId;
            searchLogs = searchRedisService.getRecentSearch(key);
        }
        return ApiResponse.onSuccess(searchLogs);
    }

    @GetMapping("/popular")
    public ApiResponse<?> getPopularSearch() {
        String memberId = getLoginMemberId();
        log.info("인기 검색어 조회 요청 : memberId = {}", memberId);
        String key1 = "popularSearches" + PostType.POST;
        String key2 = "popularSearches" + PostType.OOTD;
        List<String> searchLogs = searchRedisService.getPopularList(key1,key2);
        return ApiResponse.onSuccess(searchLogs);

    }

    @GetMapping("/tag")
    public ApiResponse<?> getSearchByTag(
            @RequestParam String tag,
            @RequestParam PostType postType,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "0") Integer size) {
        String memberId = getLoginMemberId();
        log.info("태그 필터링 조회 요청 : memberId = {}", memberId);
        if(postType.equals(PostType.POST)) return ApiResponse.onSuccess(searchService.getPostSearchByTag(tag, memberId, size, page));
        else return ApiResponse.onSuccess(searchService.getOotdSearchByTag(tag, memberId,size,page));

    }


    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
