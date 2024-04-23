package com.example.server.global.security.filter;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.security.application.JwtService;
import com.example.server.global.security.domain.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final MemberRepository memberRepository;
	private ObjectMapper objectMapper = new ObjectMapper();

	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();//5

	private final String NO_CHECK_URL = "/api/member/login";//1

	/**
	 * 1. 리프레시 토큰이 오는 경우 -> 유효하면 AccessToken 재발급후, 필터 진행 X, 바로 튕기기
	 *
	 * 2. 리프레시 토큰은 없고 AccessToken만 있는 경우 -> 유저정보 저장후 필터 계속 진행
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if(request.getRequestURI().equals(NO_CHECK_URL)) {
			filterChain.doFilter(request, response);
			return;//안해주면 아래로 내려가서 계속 필터를 진행해버림
		}

		String refreshToken = jwtService
				.extractRefreshToken(request)
				.filter(jwtService::isTokenValid)
				.orElse(null); //2


		if(refreshToken != null){
			checkRefreshTokenAndReIssueAccessToken(response, refreshToken);//3
			return;
		}

		checkAccessTokenAndAuthentication(request, response, filterChain);//4
	}

	private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).flatMap(jwtService::extractMemberId)
					.flatMap(memberRepository::findByMemberId).ifPresent(this::saveAuthentication);
			filterChain.doFilter(request, response);
		} catch (NullPointerException e) {
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.setStatus(ErrorStatus.MEMBER_EMAIL_PASSWORD_NOT_MATCH.getHttpStatus().value());
			response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onFailure(ErrorStatus.MEMBER_EMAIL_PASSWORD_NOT_MATCH.getCode(),
					ErrorStatus.MEMBER_EMAIL_PASSWORD_NOT_MATCH.getMessage(), e.getMessage())));
			log.info("Authentication failed: " + e.getClass().toString() + " : " + e.getMessage());
		}
	}


	private void saveAuthentication(Member member) {
		CustomUserDetails userDetails = CustomUserDetails.create(member);

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));


		SecurityContext context = SecurityContextHolder.createEmptyContext();//5
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}

	private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken){
		memberRepository.findByRefreshToken(refreshToken).ifPresent(member -> jwtService.sendAccessToken(response, jwtService.reIssueAccessToken(member.getMemberId(), member.getPassword())));
	}
}
