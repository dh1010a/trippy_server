package com.example.server.global.apiPayload.exception;

import org.springframework.http.HttpStatus;

public interface BaseExceptionType {
	int getErrorCode();

	HttpStatus getHttpStatus();

	String getErrorMessage();
}