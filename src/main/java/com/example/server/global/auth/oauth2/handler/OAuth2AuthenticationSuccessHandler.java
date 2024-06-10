package com.example.server.global.auth.oauth2.handler;

import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.auth.security.domain.JwtToken;
import com.example.server.global.auth.security.dto.LoginResponseDto;
import com.example.server.global.auth.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    private final MemberRepository memberRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String REFRESH_TOKEN = "refreshToken";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String memberId = extractMemberId(authentication);
        JwtToken jwtToken = jwtService.createJwtToken(authentication);

        jwtService.sendAccessToken(response, jwtToken);
        log.info( "로그인에 성공합니다. memberId: {}" , memberId);
        log.info( "AccessToken 을 발급합니다. AccessToken: {}" ,jwtToken.getAccessToken());
        log.info( "RefreshToken 을 발급합니다. RefreshToken: {}" ,jwtToken.getRefreshToken());

        SecurityContext context = SecurityContextHolder.createEmptyContext();//5
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        LoginResponseDto.LoginDto loginDto = LoginResponseDto.LoginDto.builder()
                .memberId(memberId)
                .accessToken(jwtToken.getAccessToken())
                .build();

        setCookieForLocal(response, jwtToken); // 개발단계에서 사용

        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.onSuccess(loginDto)
        ));


    }

    private void setCookieForLocal(HttpServletResponse response, JwtToken jwtToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, jwtToken.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge(60 * 60 * 24); //쿠키 만료시간 24시간
        log.info("쿠키 설정 완료");

        response.addCookie(cookie);
    }

    private void setCookieForProd(HttpServletResponse response, JwtToken jwtToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, jwtToken.getRefreshToken());
        cookie.setHttpOnly(true);  //httponly 옵션 설정
        cookie.setSecure(true); //https 옵션 설정
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge(60 * 60 * 24); //쿠키 만료시간 24시간

        response.addCookie(cookie);
    }


    private String extractMemberId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
    private String extractPassword(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getPassword();
    }
}
