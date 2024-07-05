package com.example.server.global.util;

import com.example.server.global.auth.security.domain.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtil {

	private static final String ANONYMOUS_USER = "anonymousUser";

	public static Optional<String> getLoginMemberId(){
		if (isAnonymousMember()) {
			return Optional.of(ANONYMOUS_USER);
		}
		CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return Optional.ofNullable(user.getMemberId());
	}

	public static boolean isAnonymousMember() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(ANONYMOUS_USER);
	}

}
