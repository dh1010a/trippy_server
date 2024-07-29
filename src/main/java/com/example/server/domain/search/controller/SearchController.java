package com.example.server.domain.search.controller;

import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.search.service.SearchService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @PostMapping("")
    public ApiResponse<?> saveSearchLog(@RequestBody String name) {
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(searchService.saveRecentSearch(memberId,name));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }


}
