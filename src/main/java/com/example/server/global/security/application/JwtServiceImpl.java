package com.example.server.global.security.application;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.security.domain.CustomUserDetails;
import com.example.server.global.security.domain.JwtToken;
import com.example.server.global.security.domain.JwtTokenProvider;
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

	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;

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
	public String reIssueAccessToken(String memberId, String password) {
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberId, password);
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		return jwtTokenProvider.createAccessToken(authentication);
	}

	@Override
	public String reIssueRefreshToken(String memberId, String password) {
		return jwtTokenProvider.createRefreshToken();
	}

	@Override
	public void updateRefreshToken(String memberId, JwtToken jwtToken) {
		memberRepository.findByMemberId(memberId)
				.ifPresentOrElse(
						member -> member.updateRefreshToken(jwtToken.getRefreshToken()),
						() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND)
				);
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
	public void sendAccessAndRefreshToken(HttpServletResponse response, JwtToken jwtToken) {
		response.setStatus(HttpServletResponse.SC_OK);

		setAccessTokenHeader(response, jwtToken.getAccessToken());
		setRefreshTokenHeader(response, jwtToken.getRefreshToken());

	}

	@Override
	public void sendAccessToken(HttpServletResponse response, String accessToken) {
		response.setStatus(HttpServletResponse.SC_OK);

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
		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		return Optional.ofNullable(user.getUsername());
	}

	@Override
	public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
		response.setHeader(accessHeader, accessToken);
	}

	@Override
	public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
		response.setHeader(refreshHeader, refreshToken);
	}

	@Override
	public boolean isTokenValid(String token) {
		return jwtTokenProvider.validateToken(token);
	}
}