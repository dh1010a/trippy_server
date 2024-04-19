package com.example.server.domain.member.controller;


import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("{id}")
    public ResponseEntity getUser(@PathVariable Long id) {
        log.info("readOne");
        Optional<Member> optionalUser = memberService.readOne(id);
        if (optionalUser.isPresent()) {
            Member user = optionalUser.get();
            return ResponseEntity.ok(user.getName());
        } else {
            log.info("no");
            return ResponseEntity.notFound().build();
        }
    }


}
