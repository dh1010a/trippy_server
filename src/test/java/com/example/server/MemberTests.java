package com.example.server;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Gender;
import com.example.server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

@SpringBootTest
public class MemberTests {

    @Autowired
    private MemberRepository memberRepository;

//    @Test // Member 객체 100개  생성
//    public void insertMembers(){
//
//        IntStream.rangeClosed(1,100).forEach(i -> {
//
//            Member member = Member.builder()
//                    .email("user" + i + "@aaa.com")
//                    .password("1111")
//                    .name("USER" + i)
//                    .build();
//
//            memberRepository.save(member);
//        });
//    }

//    @Test
//    public void insertMember(){
//        IntStream.rangeClosed(1,100).forEach( i->{
//            Member member = Member.builder()
//                    .email("user" + i + "@aaa.com")
//                    .password("1111")
//                    .name("USER"+i)
//                    .nickName("USERNICK"+i)
//                    .gender(Gender.FEMALE)
//                    .build();
//            memberRepository.save(member);
//        });
//    }
}
