package com.example.hotkey.likes;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikesService {

    private static final String INCREMENT_METRIC = "app.likes.increments";

    private final StringRedisTemplate redis;
    private final MeterRegistry meterRegistry;

    public LikesService(StringRedisTemplate redis, MeterRegistry meterRegistry) {
        this.redis = redis;
        this.meterRegistry = meterRegistry;
    }

    // One Redis key per video for now. increment() maps to INCR — atomic under concurrency.
    public long incrementLikes(String videoId) {
        String key = "likes:video:" + videoId;
        Long value = redis.opsForValue().increment(key);
        if (value != null) {
            // Tag "video" lets us see which ids get the most writes (Step 7 will use this idea).
            meterRegistry.counter(INCREMENT_METRIC, "video", videoId).increment();
        }
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
