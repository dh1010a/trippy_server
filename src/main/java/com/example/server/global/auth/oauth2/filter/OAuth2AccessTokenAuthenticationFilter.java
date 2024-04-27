package com.example.server.global.auth.oauth2.filter;

import com.example.server.global.auth.oauth2.AccessTokenAuthenticationProvider;
import com.example.server.global.auth.oauth2.AccessTokenSocialTypeToken;
import com.example.server.global.auth.oauth2.model.SocialType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class OAuth2AccessTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_OAUTH2_LOGIN_REQUEST_URL_PREFIX = "/api/member/login/oauth2/";

    private static final String HTTP_METHOD = "GET";

    private static final String ACCESS_TOKEN_HEADER_NAME = "Authorization";





    private static final AntPathRequestMatcher DEFAULT_OAUTH2_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_OAUTH2_LOGIN_REQUEST_URL_PREFIX +"*", HTTP_METHOD);

    public OAuth2AccessTokenAuthenticationFilter(AccessTokenAuthenticationProvider accessTokenAuthenticationProvider,
                                                 AuthenticationSuccessHandler authenticationSuccessHandler,
                                                 AuthenticationFailureHandler authenticationFailureHandler) {

        super(DEFAULT_OAUTH2_LOGIN_PATH_REQUEST_MATCHER);

        this.setAuthenticationManager(new ProviderManager(accessTokenAuthenticationProvider));

        this.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        this.setAuthenticationFailureHandler(authenticationFailureHandler);

    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        SocialType socialType = extractSocialType(request);

        String accessToken = request.getHeader(ACCESS_TOKEN_HEADER_NAME); //헤더의 AccessToken에 해당하는 값을 가져온다.
        log.info("socialType = {}",socialType.getSocialName());


        return this.getAuthenticationManager().authenticate(new AccessTokenSocialTypeToken(accessToken, socialType));
    }


    private SocialType extractSocialType(HttpServletRequest request) {//요청을 처리하는 코드이다
        return Arrays.stream(SocialType.values())//SocialType.values() -> GOOGLE, KAKAO, NAVER 가 있다.
                .filter(socialType ->
                        socialType.getSocialName()
                                .equals(request.getRequestURI().substring(DEFAULT_OAUTH2_LOGIN_REQUEST_URL_PREFIX.length())))
                //subString을 통해 문자열을 잘라주었다. 해당 코드를 실행하면 ~~~/kakao에서 kakao만 추출된다
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 URL 주소입니다"));
    }
}

