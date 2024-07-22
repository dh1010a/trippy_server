package com.example.server.global.auth.security.filter;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.security.dto.LoginResponseDto;
import com.example.server.global.auth.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SilentReAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String MATCH_URL = "/api/member/login-extension";//1

    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String HTTP_METHOD = "POST";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!request.getRequestURI().equals(MATCH_URL)) {
            filterChain.doFilter(request, response);
            return;//안해주면 아래로 내려가서 계속 필터를 진행해버림
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        if (!request.getMethod().equals(HTTP_METHOD)) {
            response.setStatus(ErrorStatus._BAD_REQUEST.getHttpStatus().value());
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(),
                    ErrorStatus._BAD_REQUEST.getMessage(), null)));
            return ;
        }

        String refreshToken = resolveRefreshToken(request);
        if (refreshToken == null) {
            response.setStatus(ErrorStatus.MEMBER_COOKIE_NOT_FOUND.getHttpStatus().value());
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onFailure(ErrorStatus.MEMBER_COOKIE_NOT_FOUND.getCode(),
                    ErrorStatus.MEMBER_COOKIE_NOT_FOUND.getMessage(), null)));
            log.info("Access Denied : RefreshToken이 없습니다. serverName : {}", request.getServerName());
        } else if (!jwtService.isTokenValid(refreshToken)) {
            response.setStatus(ErrorStatus.MEMBER_TOKEN_NOT_VALID.getHttpStatus().value());
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onFailure(ErrorStatus.MEMBER_TOKEN_NOT_VALID.getCode(),
                    ErrorStatus.MEMBER_TOKEN_NOT_VALID.getMessage(), null)));
            log.info("Access Denied : RefreshToken이 유효하지 않습니다. serverName : {}", request.getServerName());
        }

        if (refreshToken != null && jwtService.isTokenValid(refreshToken)) {
            checkRefreshTokenAndReIssueAccessToken(request, response, refreshToken);
        }



    }


    private void checkRefreshTokenAndReIssueAccessToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) throws IOException {
        Optional<Member> member = memberRepository.findByRefreshToken(refreshToken);
        if (member.isEmpty()) {
            // 유효하지만, DB에 저장된 정보와 다른 경우
            return;
        }
        log.info("RefreshToken을 재발급합니다. memberId : {} refreshToken : {}", member.get().getMemberId(), refreshToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String newRefreshToken = jwtService.reIssueAndSaveRefreshToken(member.get().getMemberId());
        String reIssuedAccessToken = jwtService.reIssueAccessToken(member.get().getMemberId());
        if (request.getServerName().equals("localhost")) {
            setCookieForLocal(response, newRefreshToken);
        } else {
            setCookieForProd(response, newRefreshToken);
        }

        LoginResponseDto.LoginDto loginDto = LoginResponseDto.LoginDto.builder()
                .memberId(member.get().getMemberId())
                .accessToken(reIssuedAccessToken)
                .role(member.get().getRole().getTitle())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onSuccess(loginDto)));

    }

    private void setCookieForLocal(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge(60 * 60 * 24); //쿠키 만료시간 24시간

        response.addCookie(cookie);
    }

    private void setCookieForProd(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);
        cookie.setHttpOnly(true);  //httponly 옵션 설정
        cookie.setSecure(true); //https 옵션 설정
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge(60 * 60 * 24); //쿠키 만료시간 24시간

        response.addCookie(cookie);
    }

    private String resolveRefreshToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) return null;
        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN.equals(cookie.getName())) {
//                log.info("cookie value = {}, {}",cookie.getValue(), cookie.getName());
                if (cookie.getValue().equals("undefined"))
                    continue;
                return cookie.getValue();
            }
        }

        return null;
    }
}
