package com.example.server.global.security.application;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.security.domain.CustomUserDetails;
import com.example.server.global.security.info.OAuth2UserInfo;
import com.example.server.global.security.info.OAuth2UserInfoFactory;
import com.example.server.global.security.model.ProviderType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private static final String SOCIAL_PASSWORD = "NO_PW";
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        try {
            return this.process(userRequest, user);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) throws Exception {
        ProviderType providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        Member saveMember = memberRepository.findByMemberId(userInfo.getId()).orElse(null);

        if (saveMember != null) {
            if (providerType != saveMember.getProviderType()) {
                throw new Exception(
                        "Looks like you're signed up with " + providerType +
                                " account. Please use your " + saveMember.getProviderType() + " account to login."
                );
            }
            updateMember(saveMember, userInfo);
        } else {
            saveMember = createMember(userInfo, providerType);
        }

        return CustomUserDetails.create(saveMember, user.getAttributes());
    }

    private Member createMember(OAuth2UserInfo userInfo, ProviderType providerType) {
        LocalDateTime now = LocalDateTime.now();
        Member member = Member.builder()
                .memberId(userInfo.getId())
                .name(userInfo.getName())
                .password(passwordEncoder.encode(SOCIAL_PASSWORD))
                .email(userInfo.getEmail())
                .profileImageUrl(userInfo.getImageUrl())
                .role(Role.ROLE_GUEST)
                .providerType(providerType)
                .build();
        memberRepository.saveAndFlush(member);
        log.info("member = " + member.getMemberId());
        log.info("member = " + member.getEmail());
        log.info("member = " + member.getIdx());

        return member;
    }

    private void updateMember(Member member, OAuth2UserInfo userInfo) {
//        if (userInfo.getEmail() != null && !member.getEmail().equals(userInfo.getEmail())) {
//            membe(userInfo.getName());
//        }

        if (userInfo.getImageUrl() != null && !member.getProfileImageUrl().equals(userInfo.getImageUrl())) {
            member.updateProfileImgUrl(userInfo.getImageUrl());
        }
    }
}
