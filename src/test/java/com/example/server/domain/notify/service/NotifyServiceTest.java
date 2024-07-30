//package com.example.server.domain.notify.service;
//
//import com.example.server.domain.member.domain.Member;
//import com.example.server.domain.member.repository.MemberRepository;
//import com.example.server.global.apiPayload.code.status.ErrorStatus;
//import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//class NotifyServiceTest {
//    @Autowired
//    NotifyService notificationService;
//
//    @Autowired
//    MemberRepository memberRepository;
//
//    @BeforeEach
//    void beforeEach() {
//    }
//
//    @Test
//    @DisplayName("알림 구독을 진행한다.")
//    public void subscribe() throws Exception {
//        //given
//        Member member = memberRepository.findByMemberId("dh1010a@gmail.com").orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
//        String lastEventId = "";
//
//        //when, then
//        Assertions.assertDoesNotThrow(() -> notificationService.subscribe(lastEventId));
//    }
//
//    @Test
//    @DisplayName("알림 메세지를 전송한다.")
//    public void send() throws Exception {
//        //given
//        Member member = testDB.findGeneralMember();
//        String lastEventId = "";
//        notificationService.subscribe(member.getId(), lastEventId);
//
//        //when, then
//        Assertions.assertDoesNotThrow(() -> notificationService.send(member, NotificationType.APPLY, "스터디 신청에 지원하셨습니다.", "localhost:8080/study/1"));
//    }
//}