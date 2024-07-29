package com.example.server.domain.search.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.search.dto.SearchRequestDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    // 최근 검색어 저장
    public String saveRecentSearch(String memberId, String name){
        if(memberId == "anonymousUser"){
            return "비회원";
        }

        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        String key = "SearchLog" + member.getMemberId();

        if(redisUtil.getSize(key) == 10) {
            redisUtil.deleteOldData(key);
        }
        redisUtil.pushSearchLog(key,name);
        return "저장";

    }
}
