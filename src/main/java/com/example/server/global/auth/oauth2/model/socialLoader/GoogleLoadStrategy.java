package com.example.server.global.auth.oauth2.model.socialLoader;

import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.auth.oauth2.model.info.GoogleOAuth2UserInfo;
import com.example.server.global.auth.oauth2.model.info.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
            log.error(ErrorStatus.KAKAO_SOCIAL_LOGIN_FAIL.getMessage() ,e.getMessage() );
            throw e;
        }
    }

    @Override
    public void unlink(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();

//            setHeaders(accessToken, headers);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            params.add("token", accessToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            restTemplate.exchange(SocialType.GOOGLE.getUnlinkUrl(),
                    SocialType.GOOGLE.getUnlinkMethod(),
                    request,
                    RESPONSE_TYPE);

        } catch (Exception e) {
            log.error(ErrorStatus.GOOGLE_SOCIAL_UNLINK_FAIL.getMessage(), e.getMessage());
            throw new ErrorHandler(ErrorStatus.GOOGLE_SOCIAL_UNLINK_FAIL);
        }
    }
}



