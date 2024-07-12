package com.example.server.domain.notify.controller;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.security.domain.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class NotifyControllerTest {
    @Autowired
    WebApplicationContext context;
    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("SSE에 연결을 진행한다.")
    public void subscribe() throws Exception {
        //given
        Member member = memberRepository.findByMemberId("dh1010a@gmail.com").orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        String accessToken = jwtTokenProvider.reIssueAccessToken(member.getMemberId());

        //when, then
        mockMvc.perform(get("/api/notify/subscribe")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Last-Event-ID", "")
                        .header("Content-Type", "text/event-stream")
                )
                .andExpect(status().isOk());
    }
}
