package com.example.server.domain.mail.converter;

import static com.example.server.domain.mail.dto.MailResponseDto.*;

public class MailDtoConverter {

    public static CheckMailResponseDto convertCheckMailResultToDto(boolean result) {
        return CheckMailResponseDto.builder()
                .isSuccess(result)
                .build();
    }

    public static CheckMailSuccessResponseDto convertCheckMailSuccessResultToDto(boolean result, String authToken) {
        return CheckMailSuccessResponseDto.builder()
                .isSuccess(result)
                .authToken(authToken)
                .build();
    }
}
