package com.example.server.domain.member.model;

import static java.util.Locale.ENGLISH;

public enum Gender {
	MALE, FEMALE;

	public static Gender fromName(String type) {
		return Gender.valueOf(type.toUpperCase(ENGLISH));
	}

}
