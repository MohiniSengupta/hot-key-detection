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

    // Redis GET — read-only; no key yet => 0.
    public long getLikes(String videoId) {
        String key = "likes:video:" + videoId;
        String raw = redis.opsForValue().get(key);
        if (raw == null || raw.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(raw);
    }
}
