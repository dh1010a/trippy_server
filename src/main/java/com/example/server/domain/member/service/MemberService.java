package com.example.server.domain.member.service;

import com.example.server.domain.blog.domain.Blog;
import com.example.server.domain.follow.repository.MemberFollowRepository;
import com.example.server.domain.follow.domain.MemberFollow;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberDtoConverter;
import com.example.server.domain.member.dto.MemberRequestDto.CreateMemberRequestDto;
import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.domain.member.dto.MemberResponseDto.*;
import com.example.server.domain.member.model.ActiveState;
import com.example.server.domain.member.model.Gender;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.auth.security.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberFollowRepository memberFollowRepository;

    private static final String DEFAULT_BIRTHDATE = "1900-01-01";

    private static final String DEFAULT_BLOG_SUFFIX = ".blog";

    public MemberTaskResultResponseDto signUp(CreateMemberRequestDto requestDto) {
        String randomNickName = requestDto.getName().substring(1, 3) + UUID.randomUUID().toString().substring(0, 9);
        if (isExistByEmail(requestDto.getEmail())) {
            throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Member member = Member.builder()
                .memberId(requestDto.getMemberId())
                .name(requestDto.getName())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickName(randomNickName)
                .email(requestDto.getEmail())
                .birthDate(LocalDate.parse(DEFAULT_BIRTHDATE, formatter))
                .phone(requestDto.getPhone())
                .gender(Gender.fromName(requestDto.getGender()))
                .activeState(ActiveState.ACTIVE)
                .role(Role.ROLE_MEMBER)
                .socialType(SocialType.LOCAL)
                .build();
        memberRepository.save(member);
        log.info("로컬 회원가입에 성공하였습니다. memberIdx = {}, memberId = {}, nickName = {}", member.getIdx(), member.getMemberId(), member.getNickName());
        return MemberDtoConverter.convertToMemberTaskDto(member);
    }

    public Member createDefaultOAuth2Member(CustomUserDetails oAuth2User) {
        String randomNickName = oAuth2User.getMemberName().substring(1, 3) + UUID.randomUUID().toString().substring(0, 9);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Member member = Member.builder()
                .memberId(oAuth2User.getMemberId())
                .name(oAuth2User.getMemberName())
                .password(oAuth2User.getPassword())
                .nickName(randomNickName)
                .email(oAuth2User.getEmail())
                .birthDate(LocalDate.parse(DEFAULT_BIRTHDATE, formatter))
                .gender(Gender.NONE)
                .role(Role.ROLE_GUEST)
                .activeState(ActiveState.ACTIVE)
                .socialType(oAuth2User.getSocialType())
                .build();
        log.info("신규 회원입니다. 등록을 진행합니다. memberId = {}, email = {}, socialType = {}", member.getMemberId(), member.getEmail(), member.getSocialType().getSocialName());
        memberRepository.save(member);
//        log.info("신규 회원 등록에 성공하였습니다. memberIdx = {}, memberId = {}", member.getIdx(), member.getMemberId());
        return member;
    }

    public IsNewMemberResponseDto isNewMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        boolean isNewMember = member.getRole() == Role.ROLE_GUEST;

        return IsNewMemberResponseDto.builder()
                .idx(member.getIdx())
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .isNewMember(isNewMember)
                .build();
    }

    public MemberInfoResponseDto getMyInfo(String memberId) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return MemberDtoConverter.convertToInfoResponseDto(member);
    }

    public MemberFollowResponseDto followMember(String memberId, String followingMemberId) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member followingMember = memberRepository.findByMemberId(followingMemberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_FOLLOWING_MEMBER_NOT_FOUND));

        if (member.getIdx().equals(followingMember.getIdx())) {
            throw new ErrorHandler(ErrorStatus.MEMBER_FOLLOWING_MYSELF);
        }
        if (memberFollowRepository.existsByMemberAndFollowingMemberIdx(member, followingMember.getIdx())) {
            throw new ErrorHandler(ErrorStatus.MEMBER_FOLLOWING_MEMBER_ALREADY_EXIST);
        }

        MemberFollow memberFollow = MemberFollow.builder()
                .member(member)
                .followingMemberIdx(followingMember.getIdx())
                .build();

        memberFollowRepository.save(memberFollow);

        member.updateMemberFollowing(memberFollow);

        return MemberDtoConverter.convertToFollowResponseDto(member, followingMember);
    }

    public MemberFollowerResponseDto getFollowerList(String memberId) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        List<MemberFollow> memberFollows = memberFollowRepository.findByFollowingMemberIdx(member.getIdx());
        List<FollowMemberInfoDto> followers = new ArrayList<>();

        for (MemberFollow memberFollow : memberFollows) {
            followers.add(MemberDtoConverter.convertToFollowMemberInfoDto(memberFollow.getMember()));
        }
        return MemberResponseDto.MemberFollowerResponseDto.builder()
                .followers(followers)
                .build();
    }

    public MemberFollowingResponseDto getFollowingList(String memberId) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        List<MemberFollow> memberFollows = member.getMemberFollows();
        List<FollowMemberInfoDto> followings = new ArrayList<>();

        for (MemberFollow memberFollow : memberFollows) {
            memberRepository.findByIdx(memberFollow.getFollowingMemberIdx())
                    .ifPresent(followingMember -> followings.add(MemberDtoConverter.convertToFollowMemberInfoDto(followingMember)));
        }
        return MemberResponseDto.MemberFollowingResponseDto.builder()
                .followings(followings)
                .build();
    }

    public String getSocialTypeByEmail(String email) {
        if (!isExistByEmail(email)) {
            return null;
        }
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return member.getSocialType().getSocialName();
    }

    public String getSocialTypeByMemberId(String memberId) {
        if (!isExistByMemberId(memberId)) {
            return null;
        }
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return member.getSocialType().getSocialName();
    }

    public boolean isExistByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean isExistByNickName(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }

    public boolean isExistByMemberId(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }

}
