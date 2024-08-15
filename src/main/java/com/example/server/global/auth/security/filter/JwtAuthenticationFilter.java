package com.example.server.global.auth.security.filter;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.auth.security.domain.JwtToken;
import com.example.server.global.auth.security.service.JwtService;
import com.example.server.global.auth.security.domain.CustomUserDetails;
import com.example.server.global.util.DeviceUtil;
import com.example.server.global.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final MemberRepository memberRepository;
	private ObjectMapper objectMapper = new ObjectMapper();
	private final RedisUtil redisUtil;

	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();//5

	private static final String NO_CHECK_URL = "/api/member/login";//1
	private static final String NO_CHECK_URL_2 = "/api/member/login/oauth2";
	private static final String NO_CHECK_URL_3 = "/api/member/login-extension";
	private static final String ACCESS_TOKEN_KEY = "accessToken";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if(request.getRequestURI().equals(NO_CHECK_URL) || request.getRequestURI().contains(NO_CHECK_URL_2) ||
				request.getRequestURI().equals(NO_CHECK_URL_3)) {
			filterChain.doFilter(request, response);
			return;//안해주면 아래로 내려가서 계속 필터를 진행해버림
		}

		checkAccessTokenAndAuthentication(request, response, filterChain);

	}

	private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			String device = DeviceUtil.getDevice(request);
			jwtService.extractAccessToken(request)
					.filter(jwtService::isTokenValid)
					.flatMap(accessToken -> {
						// Redis에서 accessToken이 존재하는지 확인
						String memberId = jwtService.extractMemberId(accessToken).orElse(null);
						if (memberId != null) {
							String redisKey = device + ACCESS_TOKEN_KEY + memberId;
							List<String> tokens = redisUtil.getAllData(redisKey);
							if (!tokens.isEmpty() && tokens.contains(accessToken)) {
								return jwtService.extractMemberId(accessToken);
							}
						}
						return Optional.empty();
					})
					.flatMap(memberRepository::findByMemberId)
					.ifPresent(this::saveAuthentication);
			filterChain.doFilter(request, response);
		} catch (NullPointerException e) {
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.setStatus(ErrorStatus.MEMBER_AUTHORIZATION_NOT_VALID.getHttpStatus().value());
			response.setContentType("application/json");
			response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.onFailure(ErrorStatus.MEMBER_AUTHORIZATION_NOT_VALID.getCode(),
					ErrorStatus.MEMBER_AUTHORIZATION_NOT_VALID.getMessage(), e.getMessage())));
			log.info("Authentication failed: " + e.getClass().toString() + " : " + e.getMessage());
		}
	}


	private void saveAuthentication(Member member) {
		CustomUserDetails userDetails = CustomUserDetails.create(member);

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));


		SecurityContext context = SecurityContextHolder.createEmptyContext();//5
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		log.info("Authentication success: memberId = {}", member.getMemberId());
	}
}
