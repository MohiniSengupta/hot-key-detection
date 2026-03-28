package com.example.hotkey.likes;

import com.example.hotkey.monitor.HotKeyTracker;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikesService {

    private static final String INCREMENT_METRIC = "app.likes.increments";

    private final StringRedisTemplate redis;
    private final MeterRegistry meterRegistry;
    private final HotKeyTracker hotKeyTracker;

    public LikesService(
            StringRedisTemplate redis,
            MeterRegistry meterRegistry,
            HotKeyTracker hotKeyTracker) {
        this.redis = redis;
        this.meterRegistry = meterRegistry;
        this.hotKeyTracker = hotKeyTracker;
    }

    // One Redis key per video for now. increment() maps to INCR — atomic under concurrency.
    public long incrementLikes(String videoId) {
        String key = "likes:video:" + videoId;
        Long value = redis.opsForValue().increment(key);
        if (value != null) {
            meterRegistry.counter(INCREMENT_METRIC, "video", videoId).increment();
            hotKeyTracker.recordWrite(videoId);
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
