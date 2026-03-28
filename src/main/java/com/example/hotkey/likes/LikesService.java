package com.example.hotkey.likes;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikesService {

    private final StringRedisTemplate redis;

    public LikesService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // One Redis key per video for now. increment() maps to INCR — atomic under concurrency.
    public long incrementLikes(String videoId) {
        String key = "likes:video:" + videoId;
        Long value = redis.opsForValue().increment(key);
        return value != null ? value : 0L;
    }
}
