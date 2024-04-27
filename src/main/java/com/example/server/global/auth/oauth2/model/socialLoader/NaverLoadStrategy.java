package com.example.server.global.auth.oauth2.model.socialLoader;

import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.auth.oauth2.model.info.NaverOAuth2UserInfo;
import com.example.server.global.auth.oauth2.model.info.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Slf4j
public class NaverLoadStrategy extends SocialLoadStrategy{


    protected OAuth2UserInfo sendRequestToSocialSite(HttpEntity request){
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(SocialType.NAVER.getUserInfoUrl(),//
                    SocialType.NAVER.getMethod(),
                    request,
                    RESPONSE_TYPE);


            return new NaverOAuth2UserInfo(response.getBody());


        } catch (Exception e) {
            log.error("AccessToken을 사용하여 NAVER 유저정보를 받아오던 중 예외가 발생했습니다 {}" ,e.getMessage() );
            throw e;
        }
    }
}
