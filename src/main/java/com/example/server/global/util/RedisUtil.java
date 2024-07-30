package com.example.server.global.util;

import com.example.server.domain.search.dto.SearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;//Redis에 접근하기 위한 Spring의 Redis 템플릿 클래스

    public String getData(String key){//지정된 키(key)에 해당하는 데이터를 Redis에서 가져오는 메서드
        ValueOperations<String,String> valueOperations=redisTemplate.opsForValue();
        return valueOperations.get(key);
    }
    public void setData(String key,String value){//지정된 키(key)에 값을 저장하는 메서드
        ValueOperations<String,String> valueOperations=redisTemplate.opsForValue();
        valueOperations.set(key,value);
    }
    public void setDataExpire(String key,String value,long duration){//지정된 키(key)에 값을 저장하고, 지정된 시간(duration) 후에 데이터가 만료되도록 설정하는 메서드
        ValueOperations<String,String> valueOperations=redisTemplate.opsForValue();
        Duration expireDuration=Duration.ofSeconds(duration);
        valueOperations.set(key,value,expireDuration);
    }
    public void deleteData(String key){//지정된 키(key)에 해당하는 데이터를 Redis에서 삭제하는 메서드
        redisTemplate.delete(key);
    }

    public Long getSize(String key){
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        if (listOperations == null) {
            return 0L;
        }
        return listOperations.size(key);
    }

    public List<String> getAllData(String key) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        Long size = getSize(key);
        if (size == null || size == 0L) {
            return List.of();
        }
        return listOperations.range(key, 0, size - 1);
    }


    public void deleteOldData(String key){
        redisTemplate.opsForList().rightPop(key);
    }

    public void pushSearchLog(String key, String name){
        redisTemplate.opsForList().leftPush(key, name);
    }

    // 검색어 횟수 증가 및 TTL 갱신 및 추가
    public void incrementCount(String key, String value, long duration) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(key, value, 1);

        // TTL 갱신
        if (redisTemplate.hasKey(key)) {
            redisTemplate.expire(key, Duration.ofSeconds(duration));
        } else {
            zSetOperations.add(key, value, 1);
            setDataExpire(key, value, duration);
        }
    }

    // 인기 검색어 횟수 조회
    public List<String> getDESCList(String key) {
        Set<String> resultSet = redisTemplate.opsForZSet().reverseRange(key, 0, 9);
        return resultSet.stream().collect(Collectors.toList());
    }




}
