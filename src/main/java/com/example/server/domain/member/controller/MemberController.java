package com.example.server.domain.member.controller;


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
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return ApiResponse.onSuccess(memberService.getMyInfo(memberId));
    }

    @GetMapping("/isNewMember")
    public ApiResponse<?> isNewMember() {
        String memberId = SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return ApiResponse.onSuccess(memberService.isNewMember(memberId));
    }

    @GetMapping("/api/member/isDuplicated")
    public ApiResponse<IsDuplicatedDto> isDuplicated(@RequestParam(value = "memberId", required = false) String memberId,
                                                     @RequestParam(value = "email", required = false) String email,
                                                     @RequestParam(value = "blogName", required = false) String blogName,
                                                     @RequestParam(value = "nickName", required = false) String nickName) throws Exception {
        IsDuplicatedDto isDuplicatedDto;
        if (memberId != null) {
            String message = "이미 가입된 내역이 존재합니다. 가입된 로그인 플랫폼 : " + memberService.getSocialTypeByEmail(email);
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByMemberId(memberId))
                    .message(message)
                    .build();
        } else if (email != null) {
            String message = "이미 가입된 내역이 존재합니다. 가입된 로그인 플랫폼 : " + memberService.getSocialTypeByEmail(email);
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByEmail(email))
                    .message(message)
                    .build();
        } else if (nickName != null) {
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByNickName(nickName))
                    .message(ErrorStatus.MEMBER_NICKNAME_ALREADY_EXIST.getMessage())
                    .build();
        }
        // 블로그 이름 중복 검사. 추후 구현 예정
//        else if (blogName != null) {
//            isDuplicatedDto = IsDuplicatedDto.builder()
//                    .isDuplicated(memberService.isExistByBlogName(blogName))
//                    .message(ErrorStatus.MEMBER_BLOGNAME_ALREADY_EXIST.getMessage())
//                    .build();
//        }

        else {
            throw new ErrorHandler(ErrorStatus._BAD_REQUEST);
        }
        return ApiResponse.onSuccess(isDuplicatedDto);

    }


}
