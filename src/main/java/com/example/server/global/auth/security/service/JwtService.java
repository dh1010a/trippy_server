package com.example.server.global.auth.security.service;

import com.example.server.global.auth.security.domain.JwtToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface JwtService {

	JwtToken createJwtToken(String memberId, String password);
	JwtToken createJwtToken(Authentication authentication);
	String reIssueAccessToken(String memberId);
	String reIssueAndSaveRefreshToken(String memberId);

	void destroyRefreshToken(String email);

	void sendAccessToken(HttpServletResponse response, JwtToken jwtToken);
	void sendReIssuedAccessToken(HttpServletResponse response, String token);

	Optional<String> extractAccessToken(HttpServletRequest request);

	Optional<String> extractRefreshToken(HttpServletRequest request);

	Optional<String> extractMemberId(String accessToken);

	void setAccessTokenHeader(HttpServletResponse response, String token);

	boolean isTokenValid(String token);

	String getDomain();

	long getAccessTokenExpirationTime();

	String createEmailAuthToken(String email);

	boolean isValidEmailAuthToken(String accessToken, String email);

}
