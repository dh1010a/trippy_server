package com.example.server.global.auth.oauth2.model.socialLoader;

import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.auth.oauth2.model.info.GoogleOAuth2UserInfo;
import com.example.server.global.auth.oauth2.model.info.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Slf4j
public class GoogleLoadStrategy extends SocialLoadStrategy{



    protected OAuth2UserInfo sendRequestToSocialSite(HttpEntity request){
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(SocialType.GOOGLE.getUserInfoUrl(),
                    SocialType.GOOGLE.getMethod(),
                    request,
                    RESPONSE_TYPE);

            return new GoogleOAuth2UserInfo(response.getBody());

        } catch (Exception e) {
            log.error("AccessToken을 사용하여 GOOGLE 유저정보를 받아오던 중 예외가 발생했습니다 {}" ,e.getMessage() );
            throw e;
        }
    }
}



