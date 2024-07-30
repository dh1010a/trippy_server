package com.example.server.domain.search.service;

import com.example.server.domain.post.model.PostType;
import com.example.server.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchRedisService {


    private final RedisUtil redisUtil;

    private static final long TTL_SECONDS = Duration.ofDays(14).toSeconds(); // TTL 2주

    public void incrementCount(String key, String value) {
        redisUtil.incrementCount(key, value, TTL_SECONDS);
    }

    public List<String> getDESCList(String key){
        return redisUtil.getDESCList(key);
    }

    public List<String> getRecentSearch(String key) {
        return redisUtil.getAllData(key);
    }

    public void saveRecentSearch(String memberId, String keyword, PostType postType) {
        String key = "SearchLog" + postType + memberId;
        if (redisUtil.getSize(key) == 10) {
            redisUtil.deleteOldData(key);
        }
        redisUtil.pushSearchLog(key, keyword);
    }

}
