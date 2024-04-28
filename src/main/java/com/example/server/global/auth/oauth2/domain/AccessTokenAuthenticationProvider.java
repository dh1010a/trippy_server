package com.example.server.global.auth.oauth2.domain;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.ActiveState;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.member.service.MemberService;
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
    private final MemberService memberService;



    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        CustomUserDetails oAuth2User = loadMemberService.getOAuth2UserDetails((AccessTokenSocialTypeToken) authentication);

        Member member = saveOrGet(oAuth2User);
        oAuth2User.updateAuthorities(member);

        return AccessTokenSocialTypeToken.builder().principal(oAuth2User).authorities(oAuth2User.getAuthorities()).build();
    }



    private Member saveOrGet(CustomUserDetails oAuth2User) {
        Member member = memberRepository.findBySocialTypeAndMemberId(oAuth2User.getSocialType(), oAuth2User.getMemberId())
                .orElse(memberService.createDefaultOAuth2Member(oAuth2User));
        log.info("회원 정보 : Idx = {}, memberId = {}, email = {}, socialType = {}",
                    member.getIdx(), member.getMemberId(), member.getEmail(), member.getSocialType().getSocialName());
        return member;
    }



    @Override
    public boolean supports(Class<?> authentication) {
        //AccessTokenSocialTypeToken타입의  authentication 객체이면 해당 Provider가 처리한다.
        return AccessTokenSocialTypeToken.class.isAssignableFrom(authentication);
    }



}

