package com.example.server.domain.bookmark.controller;

import com.example.server.domain.bookmark.service.BookMarkService;
import com.example.server.domain.member.repository.BookMarkRepository;
import com.example.server.domain.post.model.OrderType;
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

    @GetMapping
    public ApiResponse<?> getBookMarkList(@RequestParam(value = "postType") String type,
                                          @RequestParam(defaultValue = "0") Integer page,
                                          @RequestParam(defaultValue = "0") Integer size,
                                          @RequestParam(defaultValue = "LATEST") OrderType orderType) {
        String memberId = getLoginMemberId();
        log.info("북마크 리스트 조회 요청 : memberId = {}, type = {}", memberId, type);
        if (type.equals("OOTD")) {
            return ApiResponse.onSuccess(bookMarkService.getBookMarkOotdList(memberId, type, page, size, orderType));
        } else if (type.equals("POST")) {
            return ApiResponse.onSuccess(bookMarkService.getBookMarkPostList(memberId, type, page, size, orderType));
        }
        return ApiResponse.onFailure(ErrorStatus.BOOKMARK_TYPE_ERROR.getCode(), ErrorStatus.BOOKMARK_TYPE_ERROR.getMessage(), "Query parameter 'type' must be 'OOTD' or 'POST'");
    }

    @GetMapping("/my/count")
    public ApiResponse<?> getBookMarkCount() {
        String memberId = getLoginMemberId();
        log.info("내 북마크 개수 조회 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(bookMarkService.getMyBookMarkCount(memberId));
    }

    @GetMapping("/isBookMarked")
    public ApiResponse<?> isBookMarked(@RequestParam(value = "postId") Long postId) {
        String memberId = getLoginMemberId();
        log.info("북마크 여부 조회 요청 : memberId = {}, postId = {}", memberId, postId);
        return ApiResponse.onSuccess(bookMarkService.isBookMarked(memberId, postId));
    }

    @DeleteMapping
    public ApiResponse<?> deleteBookMark(@RequestParam Long postId) {
        String memberId = getLoginMemberId();
        log.info("북마크 삭제 요청 : memberId = {}, postId = {}", memberId, postId);
        return ApiResponse.onSuccess(bookMarkService.deleteBookMark(memberId, postId));
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }


}
