package com.example.server.domain.search.service;

import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.search.model.SearchType;
import com.example.server.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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


    public List<String> getPopularList(String post, String ootd){
        Set<String> postSet = redisUtil.getDESCList(post);
        Set<String> ootdSet = redisUtil.getDESCList(ootd);

        Set<String> combinedSet = new HashSet<>(postSet);;
        combinedSet.addAll(ootdSet);

        List<String> popularList = combinedSet.stream()
                .sorted(Comparator.reverseOrder())
                .limit(10)
                .collect(Collectors.toList());
        return popularList;
    }

    public List<String> getPopularListByType(PostType type){

        Set<String> postSet = redisUtil.getDESCList(type.equals(PostType.POST) ? "popularSearches" + PostType.POST : "popularSearches" + PostType.OOTD);
        return postSet.stream().sorted(Comparator.reverseOrder())
                .limit(10)
                .toList();
    }

    public List<String> getRecentSearch(String key) {
        return redisUtil.getAllData(key);
    }

    public void saveRecentSearch(String memberId, String keyword, SearchType postType) {
        String key = "SearchLog" + postType + memberId;
        if (redisUtil.getSize(key) == 10) {
            redisUtil.deleteOldData(key);
        }
        redisUtil.pushSearchLog(key, keyword);
    }

}
