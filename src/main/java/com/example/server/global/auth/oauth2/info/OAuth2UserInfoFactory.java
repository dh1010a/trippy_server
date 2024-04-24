package com.example.server.global.auth.oauth2.info;

import com.example.server.global.auth.oauth2.info.impl.GoogleOAuth2UserInfo;
import com.example.server.global.auth.oauth2.info.impl.KakaoOAuth2UserInfo;
import com.example.server.global.auth.oauth2.info.impl.NaverOAuth2UserInfo;
import com.example.server.global.auth.security.model.ProviderType;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
        switch (providerType) {
            case GOOGLE: return new GoogleOAuth2UserInfo(attributes);
            case NAVER: return new NaverOAuth2UserInfo(attributes);
            case KAKAO: return new KakaoOAuth2UserInfo(attributes);
            default: throw new IllegalArgumentException("Invalid Provider Type.");
        }
    }
}
