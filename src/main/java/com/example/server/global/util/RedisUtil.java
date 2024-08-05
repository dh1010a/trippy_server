package com.example.server.global.util;

import com.example.server.domain.search.dto.SearchRequestDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;//Redis에 접근하기 위한 Spring의 Redis 템플릿 클래스
    private final RedisTemplate<String, Object> redisObjectTemplate;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> get(String key, Class<T> type) {
        log.info("get data from redis with key: {}, type: {}", key, type.getName());
        String value = (String) redisObjectTemplate.opsForValue().get(key);
        try {
            return Optional.ofNullable(objectMapper.readValue(value, type));
        } catch (IllegalArgumentException e) {
            log.warn("value for key does not exist in redis");
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("error occurred while processing JSON", e);
            throw new ErrorHandler(ErrorStatus._JSON_PROCESSING_ERROR);
        }
    }

    public void set(String key, Object data, Long expiration) {
        log.info("set data in redis with key: {}, data: {}, expiration: {} milliseconds", key, data, expiration);
        try {
            String value = objectMapper.writeValueAsString(data);
            redisObjectTemplate.opsForValue().set(key, value, expiration, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            log.error("error occurred while processing JSON", e);
            throw new ErrorHandler(ErrorStatus._JSON_PROCESSING_ERROR);
        }
    }

    public void delete(String key) {
        log.info("delete data from redis with key: {}", key);
        redisObjectTemplate.delete(key);
    }

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
    public Set<String> getDESCList(String key) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, 9);
        //return resultSet.stream().collect(Collectors.toList());
    }




}
