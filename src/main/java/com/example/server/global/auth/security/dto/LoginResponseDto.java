package com.example.server.global.auth.security.dto;

import lombok.Builder;
import lombok.Data;

public class LoginResponseDto {

	@Data
	@Builder
	public static class LoginDto {
		public String memberId;
		public String accessToken;
		public String role;
	}

	@Data
	@Builder
	public static class ReIssueAccessTokenDto {
		public String memberId;
		public String accessToken;
	}

}
