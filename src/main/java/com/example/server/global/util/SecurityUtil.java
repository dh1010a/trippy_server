package com.example.server.global.util;

import com.example.server.global.security.domain.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtil {
	public static Optional<String> getLoginMemberId(){
		CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return Optional.ofNullable(user.getMemberId());
	}

}
