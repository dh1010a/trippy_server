package com.example.server.domain.ticket.controller;

import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.domain.ticket.service.TicketService;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticket")
public class TicketController {

    private final TicketService ticketService;

    @PatchMapping("")
    public ApiResponse<?> updateTicket(@RequestBody TicketRequestDto.UpdateTicketRequestDto requestDto) {
        String memberId = getLoginMemberId();
        log.info("티켓 업데이트 요청 : memberId = {}, ticketId = {}", memberId,requestDto.getId() );
        return ApiResponse.onSuccess(ticketService.updateTicket(requestDto));
    }
    private String getLoginMemberId() {
        return SecurityUtil.getLoginMemberId().orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
