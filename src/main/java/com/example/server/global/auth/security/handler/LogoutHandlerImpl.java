package com.example.server.global.auth.security.handler;


import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.auth.security.service.JwtService;
import com.example.server.global.util.DeviceUtil;
import com.example.server.global.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


// 추후 도입예정. 아직은 아님
@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutHandlerImpl implements LogoutHandler {


    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private static final String ACCESS_TOKEN_KEY = "accessToken";

    @Override
    @Transactional
    @SneakyThrows
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> accessToken = jwtService.extractAccessToken(request).filter(jwtService::isTokenValid);
        Optional<String> memberId = jwtService.extractMemberId(accessToken.orElse(null));
        String device = DeviceUtil.getDevice(request);


        if (accessToken.isPresent() && memberId.isPresent() && isTokenValid(accessToken.get(), memberId.get(), device)) {
            log.info("로그아웃 요청. memberId: {}", memberId.get());
            redisUtil.deleteData(device+ACCESS_TOKEN_KEY + memberId.get());

            Member member = memberRepository.getMemberById(memberId.get());
            member.updateRefreshToken(null);
            memberRepository.save(member);
            SecurityContextHolder.clearContext();
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json");
            response.setStatus(ErrorStatus._UNAUTHORIZED.getHttpStatus().value());
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(), null)));
        }
    }

    private boolean isTokenValid(String accessToken, String memberId, String device) {
        String redisKey = device + ACCESS_TOKEN_KEY + memberId;
        List<String> tokens = redisUtil.getAllData(redisKey);
        return !tokens.isEmpty() && tokens.contains(accessToken);
    }
}
