package com.example.server.domain.member.controller;


import com.example.server.domain.blog.service.BlogService;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.member.dto.MemberRequestDto.CreateMemberRequestDto;
import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.member.service.MemberService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.example.server.domain.member.dto.MemberResponseDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/signup")
    public ApiResponse<?> signUp(@RequestBody CreateMemberRequestDto createMemberRequestDto) {
        return ApiResponse.onSuccess(memberService.signUp(createMemberRequestDto));
    }

    @GetMapping
    public ApiResponse<?> getMyInfo() {
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(memberService.getMyInfo(memberId));
    }

    @GetMapping("/isNewMember")
    public ApiResponse<?> isNewMember() {
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(memberService.isNewMember(memberId));
    }

    @GetMapping("/isDuplicated")
    public ApiResponse<IsDuplicatedDto> isDuplicated(@RequestParam(value = "memberId", required = false) String memberId,
                                                     @RequestParam(value = "email", required = false) String email,
                                                     @RequestParam(value = "nickName", required = false) String nickName) throws Exception {
        IsDuplicatedDto isDuplicatedDto;
        String ALREADY_EXIST_MESSAGE = "이미 가입된 내역이 존재합니다. 가입된 로그인 플랫폼 : ";

        if (memberId != null) {
            String message = ALREADY_EXIST_MESSAGE + memberService.getSocialTypeByEmail(email);
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByMemberId(memberId))
                    .message(memberService.isExistByMemberId(memberId)? message : "사용 가능한 아이디입니다.")
                    .build();
        } else if (email != null) {
            String message = ALREADY_EXIST_MESSAGE + memberService.getSocialTypeByEmail(email);
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByEmail(email))
                    .message(memberService.isExistByEmail(email)? message : "사용 가능한 이메일입니다.")
                    .build();
        } else if (nickName != null) {
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByNickName(nickName))
                    .message(memberService.isExistByNickName(nickName) ? ErrorStatus.MEMBER_NICKNAME_ALREADY_EXIST.getMessage()
                            : "사용 가능한 닉네임입니다.")
                    .build();
        } else {
            throw new ErrorHandler(ErrorStatus._BAD_REQUEST);
        }
        return ApiResponse.onSuccess(isDuplicatedDto);

    }

    @PostMapping("/follow")
    public ApiResponse<?> followMember(@RequestParam(value = "memberId", required = false) String followingMemberId) {
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(memberService.followMember(memberId, followingMemberId));
    }

    @GetMapping("/follow")
    public ApiResponse<?> getFollow(@RequestParam(value = "type") String type)  {
        // 비활성화된 멤버는 조회 안되게 하는 로직 추가 구현 해야함
        String memberId = getLoginMemberId();
        if (type.equals("follower")) {
            return ApiResponse.onSuccess(memberService.getFollowerList(memberId));
        } else if (type.equals("following")) {
            return ApiResponse.onSuccess(memberService.getFollowingList(memberId));
        }

        return ApiResponse.onSuccess(ErrorStatus._BAD_REQUEST);
    }

    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }



}
