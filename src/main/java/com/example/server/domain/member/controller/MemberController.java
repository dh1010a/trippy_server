package com.example.server.domain.member.controller;


import com.example.server.domain.member.dto.MemberDtoConverter;
import com.example.server.domain.member.dto.MemberRequestDto;
import com.example.server.domain.member.dto.MemberRequestDto.CommonCreateMemberRequestDto;
import com.example.server.domain.member.dto.MemberRequestDto.CreateMemberRequestDto;
import com.example.server.domain.member.service.MemberService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.example.server.domain.member.dto.MemberResponseDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ApiResponse<?> signUp(@RequestBody CreateMemberRequestDto createMemberRequestDto) {
        log.info("회원가입 요청 : memberId = {}", createMemberRequestDto.getMemberId());
        return ApiResponse.onSuccess(memberService.signUp(createMemberRequestDto));
    }

    @PostMapping("/signup/common")
    public ApiResponse<?> commonSignUp(@RequestBody CommonCreateMemberRequestDto commonCreateMemberRequestDto) throws Exception{
        String loginMemberId = getLoginMemberId();
        log.info("공통 회원가입 요청 : memberId = {}", loginMemberId);
        return ApiResponse.onSuccess(memberService.commonSignUp(commonCreateMemberRequestDto, loginMemberId));
    }



    @GetMapping
    public ApiResponse<?> getMyInfo() {
        String myId = getLoginMemberId();
        log.info("내 정보 조회 요청 : memberId = {}", myId);
        return ApiResponse.onSuccess(memberService.getMyInfo(myId));
    }

    @GetMapping("/profile")
    public ApiResponse<?> getMemberInfo(@RequestParam("memberId") String memberId) {
        log.info("요청자 memberId = {}, 요청 대상 memberId = {}", getLoginMemberId() ,memberId);
        // 추후 비활성 유저도 접근하지 못하도록 로직 구성해야함
        if (memberService.isGuestRole(memberId)) {
            return ApiResponse.onFailure(ErrorStatus.MEMBER_PROFILE_ACCESS_DENY.getCode(), ErrorStatus.MEMBER_PROFILE_ACCESS_DENY.getMessage(),
                    "프로필을 읽어올 수 없는 유저입니다.");
        }
        return ApiResponse.onSuccess(memberService.getMemberInfo(memberId));
    }

    @PatchMapping
    public ApiResponse<?> updateMyInfo(@RequestBody MemberRequestDto.UpdateMemberRequestDto updateMemberRequestDto) throws Exception {
        String memberId = getLoginMemberId();
        log.info("내 정보 수정 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(memberService.updateMyInfo(memberId, updateMemberRequestDto));
    }

    @GetMapping("/isNewMember")
    public ApiResponse<?> isNewMember() {
        String memberId = getLoginMemberId();
        return ApiResponse.onSuccess(memberService.isNewMember(memberId));
    }

    @GetMapping("/isDuplicated")
    public ApiResponse<?> isDuplicated(@RequestParam(value = "memberId", required = false) String memberId,
                                                     @RequestParam(value = "email", required = false) String email,
                                                     @RequestParam(value = "nickName", required = false) String nickName,
                                                     @RequestParam(value = "blogName", required = false) String blogName) throws Exception {
        IsDuplicatedDto isDuplicatedDto;
        String ALREADY_EXIST_MESSAGE = "이미 가입된 내역이 존재합니다. 가입된 로그인 플랫폼 : ";

        log.info("중복 조회 요청. memberId = {}, email = {}, nickName = {}, blogName = {}", memberId, email, nickName, blogName);

        if (memberId != null ) {
            String message = ALREADY_EXIST_MESSAGE + memberService.getSocialTypeByMemberId(email);
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByMemberId(memberId))
                    .message(memberService.isExistByMemberId(memberId)? message : "사용 가능한 아이디입니다.")
                    .build();
        } else if (email != null) {
            String message = ALREADY_EXIST_MESSAGE + memberService.getSocialTypeByEmail(email);
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByEmail(email))
                    .message(memberService.isExistByMemberId(email)? message : "사용 가능한 이메일입니다.")
                    .build();
        } else if (nickName != null) {
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByNickName(nickName))
                    .message(memberService.isExistByNickName(nickName) ? ErrorStatus.MEMBER_NICKNAME_ALREADY_EXIST.getMessage()
                            : "사용 가능한 닉네임입니다.")
                    .build();
        } else if (blogName != null) {
            isDuplicatedDto = IsDuplicatedDto.builder()
                    .isDuplicated(memberService.isExistByBlogName(blogName))
                    .message(memberService.isExistByBlogName(blogName) ? ErrorStatus.MEMBER_BLOG_NAME_ALREADY_EXIST.getMessage()
                            : "사용 가능한 블로그 이름입니다.")
                    .build();
        } else {
            return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(), ErrorStatus._BAD_REQUEST.getMessage(),
                    "요청 파라미터가 잘못되었습니다.");
        }
        return ApiResponse.onSuccess(isDuplicatedDto);

    }

    @PostMapping("/follow")
    public ApiResponse<?> followMember(@RequestParam(value = "memberId") String followingMemberId) {
        String memberId = getLoginMemberId();
        log.info("팔로우 요청 : memberId = {}, followingMemberId = {}", memberId, followingMemberId);
        return ApiResponse.onSuccess(memberService.followMember(memberId, followingMemberId));
    }

    @GetMapping("/following")
    public ApiResponse<?> getFollowing(@RequestParam("memberId") String targetMemberId){
        // 비활성화된 멤버는 조회 안되게 하는 로직 추가 구현 해야함
        String memberId = getLoginMemberId();
        log.info("팔로잉 조회 요청 : memberId = {}, targetMemberId = {}", memberId, targetMemberId);
        if (memberService.isNotValidAccessToFollow(memberId, targetMemberId)) {
            return ApiResponse.onFailure(ErrorStatus.MEMBER_CANNOT_ACCESS_FOLLOW.getCode(), ErrorStatus.MEMBER_CANNOT_ACCESS_FOLLOW.getMessage(),
                    "팔로잉 목록에 접근할 수 없습니다.");
        }
        return ApiResponse.onSuccess(memberService.getFollowingList(targetMemberId));
    }

    @GetMapping("/follower")
    public ApiResponse<?> getFollower(@RequestParam("memberId") String targetMemberId)  {
        // 비활성화된 멤버는 조회 안되게 하는 로직 추가 구현 해야함
        String memberId = getLoginMemberId();
        log.info("팔로우 조회 요청 : memberId = {}, targetMemberId = {}", memberId, targetMemberId);
        if (memberService.isNotValidAccessToFollow(memberId, targetMemberId)) {
            return ApiResponse.onFailure(ErrorStatus.MEMBER_CANNOT_ACCESS_FOLLOW.getCode(), ErrorStatus.MEMBER_CANNOT_ACCESS_FOLLOW.getMessage(),
                    "팔로잉 목록에 접근할 수 없습니다.");
        }
        return ApiResponse.onSuccess(memberService.getFollowerList(targetMemberId));
    }

    @DeleteMapping("/follow")
    public ApiResponse<?> deleteFollow(@RequestParam(value = "type") String type,
                                      @RequestParam(value = "targetMemberId") String targetMemberId) {
        String memberId = getLoginMemberId();
        if (type.equals("follower")) {
            return ApiResponse.onSuccess(memberService.deleteFollower(memberId, targetMemberId));
        }
        else if (type.equals("following")) {
            return ApiResponse.onSuccess(memberService.unFollow(memberId, targetMemberId));
        }
        return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(), ErrorStatus._BAD_REQUEST.getMessage(),
                "type 형식이 잘못되었습니다.");
    }

    @GetMapping("/follow/available")
    public ApiResponse<?> isAvailableToGetFollow(@RequestParam(value = "memberId") String targetMemberId) {
        String memberId = getLoginMemberId();
        log.info("팔로우 가능 여부 조회 요청 : memberId = {}, targetMemberId = {}", memberId, targetMemberId);
        return ApiResponse.onSuccess(MemberDtoConverter.convertToMemberGetFollowAvailableResponseDto(!memberService.isNotValidAccessToFollow(memberId, targetMemberId)));
    }

    @PatchMapping("/password")
    public ApiResponse<?> changePassword(@RequestBody MemberRequestDto.ChangePasswordRequestDto requestDto,
                                         @RequestParam(value = "code") String code) {
        log.info("비밀번호 변경 요청 : memberId = {}", requestDto.getEmail());
        return ApiResponse.onSuccess(memberService.changePassword(requestDto, code));
    }

    @PostMapping("/interest")
    public ApiResponse<?> updateInterestedTypes(@RequestBody MemberRequestDto.UpdateInterestedTypesRequestDto requestDto) {
        String memberId = getLoginMemberId();
        log.info("관심사 변경 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(memberService.setInterestedTypes(memberId, requestDto));
    }

    @GetMapping("/find")
    public ApiResponse<?> findEmailByNickName(@RequestParam(value = "nickName") String nickName) {
        log.info("이메일 찾기 요청 : nickName = {}", nickName);
        return ApiResponse.onSuccess(memberService.findEmailByNickName(nickName));
    }

    @GetMapping("/bookmark")
    public ApiResponse<?> getBookmarkList() {
        String memberId = getLoginMemberId();
        log.info("북마크 조회 요청 : memberId = {}", memberId);
        return ApiResponse.onSuccess(memberService.getBookmarkList(memberId));
    }



    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }


}
