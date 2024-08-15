package com.example.server.global.auth.security.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.security.domain.JwtToken;
import com.example.server.global.auth.security.domain.JwtTokenProvider;
import com.example.server.global.auth.security.domain.CustomUserDetails;
import com.example.server.global.auth.security.dto.LoginResponseDto;
import com.example.server.global.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
@Setter(value = AccessLevel.PRIVATE)
@Slf4j
public class JwtServiceImpl implements JwtService {

	private final MemberRepository memberRepository;
	private final ObjectMapper objectMapper;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final RedisUtil redisUtil;

	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;

	@Value("${jwt.domain}")
	private String domain;

	private static final String BEARER = "Bearer ";

	//== 메서드 ==//
	@Override
	public JwtToken createJwtToken(String memberId, String password) {
		// 1. username + password 를 기반으로 Authentication 객체 생성
		// 이때 authentication 은 인증 여부를 확인하는 authenticated 값이 false
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberId, password);

		// 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member 에 대한 검증 진행
		// authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드 실행
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

		// 3. 인증 정보를 기반으로 JWT 토큰 생성
		return jwtTokenProvider.createToken(authentication);
	}

	@Override
	public JwtToken createJwtToken(Authentication authentication) {
		return jwtTokenProvider.createToken(authentication);
	}

	@Override
	public String reIssueAccessToken(String memberId) {
		return jwtTokenProvider.reIssueAccessToken(memberId);
	}

	@Override
	public String reIssueAndSaveRefreshToken(String memberId) {
		String refreshToken =  jwtTokenProvider.createRefreshToken(memberId);
		memberRepository.findByMemberId(memberId)
				.ifPresentOrElse(
						member -> member.updateRefreshToken(refreshToken),
						() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND)
				);
		return refreshToken;
	}

	@Override
	public void destroyRefreshToken(String username) {
		memberRepository.findByMemberId(username)
				.ifPresentOrElse(
						Member::destroyRefreshToken,
						() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND)
				);
	}

	@Override
	public void sendAccessToken(HttpServletResponse response, JwtToken jwtToken) {
		response.setStatus(HttpServletResponse.SC_OK);

		setAccessTokenHeader(response, jwtToken.getAccessToken());
//		setRefreshTokenHeader(response, jwtToken.getRefreshToken());

	}

	@Override
	public void sendReIssuedAccessToken(HttpServletResponse response, String accessToken)  {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		String memberId = extractMemberId(accessToken).orElse(null);

		LoginResponseDto.ReIssueAccessTokenDto loginDto = LoginResponseDto.ReIssueAccessTokenDto.builder()
				.memberId(memberId)
				.accessToken(accessToken)
				.build();

		log.info("AccessToken을 재발급 합니다. memberId: {}, AccessToken = {}", memberId, accessToken);

		try {
			response.getWriter().write(objectMapper.writeValueAsString(
					ApiResponse.onSuccess(loginDto)
			));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setAccessTokenHeader(response, accessToken);
	}

	@Override
	public Optional<String> extractAccessToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessHeader)).filter(
				accessToken -> accessToken.startsWith(BEARER)
		).map(accessToken -> accessToken.replace(BEARER, ""));
	}

	@Override
	public Optional<String> extractRefreshToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(refreshHeader)).filter(
				refreshToken -> refreshToken.startsWith(BEARER)
		).map(refreshToken -> refreshToken.replace(BEARER, ""));
	}

	@Override
	public Optional<String> extractMemberId(String accessToken) {
		if (accessToken == null) {
			return Optional.empty();
		}
		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		return Optional.ofNullable(user.getUsername());
	}

	@Override
	public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
		response.setHeader(accessHeader, accessToken);
	}

	@Override
	public boolean isTokenValid(String token) {
		return jwtTokenProvider.validateToken(token);
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public long getAccessTokenExpirationTime() {
		return jwtTokenProvider.getAccessTokenExpirationTime();
	}

}