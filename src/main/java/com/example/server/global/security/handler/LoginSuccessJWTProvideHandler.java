package com.example.server.global.security.handler;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.config.AppProperties;
import com.example.server.global.dto.LoginResponseDto.LoginDto;
import com.example.server.global.security.application.JwtService;
import com.example.server.global.security.domain.JwtToken;
import com.example.server.global.security.info.OAuth2UserInfo;
import com.example.server.global.security.info.OAuth2UserInfoFactory;
import com.example.server.global.security.model.ProviderType;
import com.example.server.global.security.repository.HttpCookieOAuthAuthorizationRequestRepository;
import com.example.server.global.util.CookieUtil;
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
public class LoginSuccessJWTProvideHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtService jwtService;

	private final MemberRepository memberRepository;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws IOException, ServletException {
		String memberId = extractMemberId(authentication);
		JwtToken jwtToken = jwtService.createJwtToken(authentication);

		jwtService.sendAccessAndRefreshToken(response, jwtToken);
		log.info( "로그인에 성공합니다. memberId: {}" , memberId);
		log.info( "AccessToken 을 발급합니다. AccessToken: {}" ,jwtToken.getAccessToken());
		log.info( "RefreshToken 을 발급합니다. RefreshToken: {}" ,jwtToken.getRefreshToken());

		SecurityContext context = SecurityContextHolder.createEmptyContext();//5
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		LoginDto loginDto = LoginDto.builder()
				.memberId(memberId)
				.accessToken(jwtToken.getAccessToken())
				.refreshToken(jwtToken.getRefreshToken())
				.build();

		response.getWriter().write(objectMapper.writeValueAsString(
				ApiResponse.onSuccess(loginDto)
		));
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
