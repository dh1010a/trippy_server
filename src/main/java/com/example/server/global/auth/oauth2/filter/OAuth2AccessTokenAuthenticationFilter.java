package com.example.server.global.auth.oauth2.filter;

import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.oauth2.domain.AccessTokenAuthenticationProvider;
import com.example.server.global.auth.oauth2.domain.AccessTokenSocialTypeToken;
import com.example.server.global.auth.oauth2.model.SocialType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@Slf4j
public class OAuth2AccessTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_OAUTH2_LOGIN_REQUEST_URL_PREFIX = "/api/member/login/oauth2/";

    private static final String HTTP_METHOD = "POST";

    private static final String ACCESS_TOKEN_HEADER_NAME = "Authorization";

    private static final String ACCESS_TOKEN_PREFIX= "Bearer ";





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
        SocialType socialType = extractSocialType(request, response);
        try {

//          String accessToken = request.getHeader(ACCESS_TOKEN_HEADER_NAME).substring(ACCESS_TOKEN_PREFIX.length()); // Bearer이 두번 붙게되어, 최초 한번은 제외 (POSTMAN으로 테스트 시)
            String accessToken = request.getHeader(ACCESS_TOKEN_HEADER_NAME); // Bearer이 두번 붙게되어, 최초 한번은 제외 (POSTMAN으로 테스트 시)

            return this.getAuthenticationManager().authenticate(new AccessTokenSocialTypeToken(accessToken, socialType));

        } catch (Exception e) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.setStatus(ErrorStatus.SOCIAL_UNAUTHORIZED.getHttpStatus().value());
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onFailure(ErrorStatus.SOCIAL_UNAUTHORIZED.getCode(),
                    socialType.getSocialName() + " " + ErrorStatus.SOCIAL_UNAUTHORIZED.getMessage(), e.getMessage())));
            log.info("{} Authentication failed: " + e.getClass().toString() + " : " + e.getMessage(), socialType.getSocialName());
            log.info("{}", e.getStackTrace());
            return null;
        }
    }


    private SocialType extractSocialType(HttpServletRequest request, HttpServletResponse response) {
        log.info(request.getRequestURI().substring(DEFAULT_OAUTH2_LOGIN_REQUEST_URL_PREFIX.length()));

        return Arrays.stream(SocialType.values())
                .filter(socialType ->
                        socialType.getSocialName()
                                .equals(request.getRequestURI().substring(DEFAULT_OAUTH2_LOGIN_REQUEST_URL_PREFIX.length())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 URL 주소입니다"));
    }
}

