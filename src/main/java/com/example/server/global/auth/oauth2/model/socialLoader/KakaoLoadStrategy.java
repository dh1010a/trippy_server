package com.example.server.global.auth.oauth2.model.socialLoader;

import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.oauth2.model.SocialType;
import com.example.server.global.auth.oauth2.model.info.KakaoOAuth2UserInfo;
import com.example.server.global.auth.oauth2.model.info.OAuth2UserInfo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Slf4j
@Transactional
public class KakaoLoadStrategy extends SocialLoadStrategy{



    protected OAuth2UserInfo sendRequestToSocialSite(HttpEntity request){
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(SocialType.KAKAO.getUserInfoUrl(),// -> /v2/user/me
                    SocialType.KAKAO.getMethod(),
                    request,
                    RESPONSE_TYPE);

            return new KakaoOAuth2UserInfo(response.getBody());

        } catch (Exception e) {
            log.error(ErrorStatus.KAKAO_SOCIAL_LOGIN_FAIL.getMessage(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void unlink(String memberId, String adminKey) {
        try {
            // Headers 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  // Content-Type 설정
            headers.set("Authorization", "KakaoAK " + adminKey);  // Authorization 헤더 추가
            log.info("Kakao adminKey : " + adminKey);

            // 파라미터 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("target_id_type", "user_id");
            params.add("target_id", memberId);  // 실제 user_id 값을 설정

            // 요청 생성
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 요청 보내기
            restTemplate.exchange(SocialType.KAKAO.getUnlinkUrl(),
                    SocialType.KAKAO.getUnlinkMethod(),
                    request,
                    RESPONSE_TYPE);

        } catch (Exception e) {
            log.error(ErrorStatus.KAKAO_SOCIAL_UNLINK_FAIL.getMessage() + e.getMessage());
            throw new ErrorHandler(ErrorStatus.KAKAO_SOCIAL_UNLINK_FAIL);
        }
    }
}

