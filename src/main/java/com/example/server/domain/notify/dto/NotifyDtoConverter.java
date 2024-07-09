package com.example.server.domain.notify.dto;

import com.example.server.domain.notify.domain.Notify;

public class NotifyDtoConverter {

    public static NotifyDto.Response convertToResponseDto(Notify notify) {
        return NotifyDto.Response.builder()
                .content(notify.getContent())
                .id(notify.getId().toString())
                .nickName(notify.getReceiver().getNickName())
                .createdAt(notify.getCreatedAt().toString())
                .build();

    }

}
