package com.ivan_degtev.telegrambotforpapablinov.service.impl;

import com.ivan_degtev.telegrambotforpapablinov.service.UpdateIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateIdServiceImpl implements UpdateIdService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "update_id:";

    public boolean isUniqueUpdateId(Long updateId) {
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + updateId, "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(isNew);
    }
}
