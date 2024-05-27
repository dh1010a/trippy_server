package com.example.server.global.auth.security.handler;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.auth.security.dto.LoginResponseDto.LoginDto;
import com.example.server.global.auth.security.service.JwtService;
import com.example.server.global.auth.security.domain.JwtToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
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

		Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
		member.updateRefreshToken(jwtToken.getRefreshToken());

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
				.role(member.getRole().getTitle())
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
