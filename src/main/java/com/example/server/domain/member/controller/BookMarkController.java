package com.example.server.domain.member.controller;

import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.member.repository.BookMarkRepository;
import com.example.server.domain.member.service.BookMarkService;
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
@RequestMapping("/api/bookmark")
public class BookMarkController {

    private final BookMarkRepository bookMarkRepository;
    private final BookMarkService bookMarkService;

    @PostMapping("")
    public ApiResponse<?> postBookMark(@RequestParam Long postId) {
        String memberId = getLoginMemberId();
        log.info("북마크 추가 요청 : memberId = {}", getLoginMemberId());
        return  ApiResponse.onSuccess(bookMarkService.postBookMark(postId, memberId));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }


}
