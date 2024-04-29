package com.example.server.domain.post.controller;

import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.service.PostService;
import com.example.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
//
//    @PostMapping("")
//    public ApiResponse<?> signUp(@RequestBody PostRequestDto.u createMemberRequestDto) {
//        return ApiResponse.onSuccess();
//    }


}
