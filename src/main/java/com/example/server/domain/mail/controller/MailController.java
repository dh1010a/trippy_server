package com.example.server.domain.mail.controller;


import com.example.server.domain.mail.service.MailService;
import com.example.server.domain.mail.dto.MailDto;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.member.service.MemberService;
import com.example.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
//@RequestMapping("/api/email")
@RestController
public class MailController {

    private final MailService mailService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;


    @PostMapping("/api/email/send")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<?> sendMail(@RequestParam(value = "email") String email) {
        return ApiResponse.onSuccess(mailService.sendEmail(email));
    }

    @PostMapping("/api/email/confirm")
    public ApiResponse<?> confirmEmail(@RequestBody @Valid MailDto.CheckMailRequestDto dto) {
        return ApiResponse.onSuccess(mailService.checkEmail(dto.getEmail(), dto.getAuthNumber()));
    }

}
