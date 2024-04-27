package com.example.server.global.auth.oauth2.socialLoader;

import com.example.server.global.auth.oauth2.model.SocialType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Slf4j
public class NaverLoadStrategy extends SocialLoadStrategy{


    protected String sendRequestToSocialSite(HttpEntity request){
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(SocialType.NAVER.getUserInfoUrl(),//
                    SocialType.NAVER.getMethod(),
                    request,
                    RESPONSE_TYPE);


            Map<String , Object> response2 = ( Map<String , Object>)response.getBody().get("response");
            return response2.get("id").toString();


        } catch (Exception e) {
            log.error("AccessToken을 사용하여 NAVER 유저정보를 받아오던 중 예외가 발생했습니다 {}" ,e.getMessage() );
            throw e;
        }
    }
}
