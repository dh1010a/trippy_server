package com.example.server.global.auth.oauth2.service;

import com.example.server.global.auth.oauth2.domain.AccessTokenSocialTypeToken;
import com.example.server.global.auth.oauth2.model.info.OAuth2UserInfo;
import com.example.server.global.auth.oauth2.model.socialLoader.GoogleLoadStrategy;
import com.example.server.global.auth.oauth2.model.socialLoader.KakaoLoadStrategy;
import com.example.server.global.auth.oauth2.model.socialLoader.NaverLoadStrategy;
import com.example.server.global.auth.oauth2.model.socialLoader.SocialLoadStrategy;
import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.auth.security.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LoadMemberService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PasswordEncoder passwordEncoder;

    private static final String OAUTH2_USER_PASSWORD = "TRIPPY_OAUTH2_PW";


    public CustomUserDetails getOAuth2UserDetails(AccessTokenSocialTypeToken authentication)  {

        SocialType socialType = authentication.getSocialType();

        SocialLoadStrategy socialLoadStrategy = getSocialLoadStrategy(socialType);//SocialLoadStrategy 설정

        OAuth2UserInfo userInfo = socialLoadStrategy.getUserInfo(authentication.getAccessToken());//PK 가져오기

        return CustomUserDetails.builder() //PK와 SocialType을 통해 회원 생성
                .id(userInfo.getId())
                .email(userInfo.getEmail())
                .password(passwordEncoder.encode(OAUTH2_USER_PASSWORD))
                .name(userInfo.getName())
                .socialType(socialType)
                .build();
    }

    private SocialLoadStrategy getSocialLoadStrategy(SocialType socialType) {
        return switch (socialType){

            case KAKAO -> new KakaoLoadStrategy();
            case GOOGLE ->  new GoogleLoadStrategy();
            case NAVER ->  new NaverLoadStrategy();
            default -> throw new IllegalArgumentException("지원하지 않는 로그인 형식입니다");
        };
    }


}

