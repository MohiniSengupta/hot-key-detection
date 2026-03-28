package com.example.hotkey.load;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.hotkey.likes.LikesService;

@RestController
@RequestMapping("/api/load")
public class LoadController {

    private static final int MAX_COUNT = 50_000;

    private final LikesService likesService;

    public LoadController(LikesService likesService) {
        this.likesService = likesService;
    }

    // Tight loop: all increments use the same Redis key (hot-key stress, no external load tool).
    @PostMapping("/videos/{videoId}/likes")
    public Map<String, Object> burstLikes(
            @PathVariable String videoId,
            @RequestParam(defaultValue = "100") int count) {
        if (count < 1 || count > MAX_COUNT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "count must be between 1 and " + MAX_COUNT);
        }
        long startNanos = System.nanoTime();
        for (int i = 0; i < count; i++) {
            likesService.incrementLikes(videoId);
        }
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        long likesAfter = likesService.getLikes(videoId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("videoId", videoId);
        body.put("increments", count);
        body.put("likesAfter", likesAfter);
        body.put("durationMs", durationMs);
        return body;
    }
}
