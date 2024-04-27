package com.example.server.global.auth.oauth2;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.auth.oauth2.service.LoadMemberService;
import com.example.server.global.auth.security.domain.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Transactional
public class AccessTokenAuthenticationProvider implements AuthenticationProvider {//AuthenticationProvider을 구현받아 authenticate와 supports를 구현해야 한다.

    private final LoadMemberService loadMemberService;  //restTemplate를 통해서 AccessToken을 가지고 회원의 정보를 가져오는 역할을 한다.
    private final MemberRepository memberRepository;//받아온 정보를 통해 DB에서 회원을 조회하는 역할을 한다.



    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {//ProviderManager가 호출한다. 인증을 처리한다

        CustomUserDetails oAuth2User = loadMemberService.getOAuth2UserDetails((AccessTokenSocialTypeToken) authentication);
        //OAuth2UserDetails는  UserDetails를 상속받아 구현한 클래스이다. 이후 일반 회원가입 시 UserDetails를 사용하는 부분과의 다형성을 위해 이렇게 구현하였다.
        //getOAuth2UserDetails에서는 restTemplate과 AccessToken을 가지고 회원정보를 조회해온다 (식별자 값을 가져옴)


        Member member = saveOrGet(oAuth2User);//받아온 식별자 값과 social로그인 방식을 통해 회원을 DB에서 조회 후 없다면 새로 등록해주고, 있다면 그대로 반환한다.
        oAuth2User.updateAuthorities(member);//우리의 Role의 name은 ADMIN, USER, GUEST로 ROLE_을 붙여주는 과정이 필요하다. setRolse가 담당한다.

        return AccessTokenSocialTypeToken.builder().principal(oAuth2User).authorities(oAuth2User.getAuthorities()).build();
        //AccessTokenSocialTypeToken객체를 반환한다. principal은 OAuth2UserDetails객체이다. (formLogin에서는 UserDetails를 가져와서 결국 ContextHolder에 저장하기 때문에)
        //이렇게 구현하면 UserDetails 타입으로 회원의 정보를 어디서든 조회할 수 있다.
    }



    private Member saveOrGet(CustomUserDetails oAuth2User) {
        return memberRepository.findBySocialTypeAndMemberId(oAuth2User.getSocialType(),
                        oAuth2User.getMemberId())
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .socialType(oAuth2User.getSocialType())
                        .memberId(oAuth2User.getMemberId())
                        .role(Role.ROLE_GUEST).build()));
    }



    @Override
    public boolean supports(Class<?> authentication) {
        return AccessTokenSocialTypeToken.class.isAssignableFrom(authentication); //AccessTokenSocialTypeToken타입의  authentication 객체이면 해당 Provider가 처리한다.
    }



}

