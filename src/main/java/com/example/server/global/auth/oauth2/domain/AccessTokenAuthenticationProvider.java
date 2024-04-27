package com.example.server.global.auth.oauth2.domain;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.ActiveState;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.auth.oauth2.service.LoadMemberService;
import com.example.server.global.auth.security.domain.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Transactional
@Slf4j
public class AccessTokenAuthenticationProvider implements AuthenticationProvider {

    private final LoadMemberService loadMemberService;
    private final MemberRepository memberRepository;



    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        CustomUserDetails oAuth2User = loadMemberService.getOAuth2UserDetails((AccessTokenSocialTypeToken) authentication);

        Member member = saveOrGet(oAuth2User);
        oAuth2User.updateAuthorities(member);

        return AccessTokenSocialTypeToken.builder().principal(oAuth2User).authorities(oAuth2User.getAuthorities()).build();
    }



    private Member saveOrGet(CustomUserDetails oAuth2User) {
        Member member = memberRepository.findBySocialTypeAndMemberId(oAuth2User.getSocialType(), oAuth2User.getMemberId()).orElse(null);
        if (member == null) {
            member = Member.builder()
                    .socialType(oAuth2User.getSocialType())
                    .memberId(oAuth2User.getMemberId())
                    .email(oAuth2User.getEmail())
                    .password(oAuth2User.getPassword())
                    .role(Role.ROLE_GUEST)
                    .activeState(ActiveState.ACTIVE)
                    .build();
            log.info("신규 회원입니다. 등록을 진행합니다. memberId = {}, email = {}, socialType = {}", member.getMemberId(), member.getEmail(), member.getSocialType().getSocialName());
            memberRepository.save(member);
            log.info("신규 회원 등록에 성공하였습니다. memberIdx = {}", member.getIdx());
        } else {
            log.info("기존 회원입니다. 회원 정보를 가져옵니다. Idx = {}, memberId = {}, email = {}, socialType = {}",
                    member.getIdx(), member.getMemberId(), member.getEmail(), member.getSocialType().getSocialName());
        }
        return member;
//        return memberRepository.findBySocialTypeAndMemberId(oAuth2User.getSocialType(),
//                        oAuth2User.getMemberId())
//                .orElseGet(() -> memberRepository.save(Member.builder()
//                        .socialType(oAuth2User.getSocialType())
//                        .memberId(oAuth2User.getMemberId())
//                        .email(oAuth2User.getEmail())
//                        .password(oAuth2User.getPassword())
//                        .role(Role.ROLE_GUEST)
//                        .activeState(ActiveState.ACTIVE)
//                        .build())
//                );
    }



    @Override
    public boolean supports(Class<?> authentication) {
        //AccessTokenSocialTypeToken타입의  authentication 객체이면 해당 Provider가 처리한다.
        return AccessTokenSocialTypeToken.class.isAssignableFrom(authentication);
    }



}

