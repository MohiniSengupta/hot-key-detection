package com.example.hotkey.likes;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/videos")
public class LikesController {

    private final LikesService likesService;

    public LikesController(LikesService likesService) {
        this.likesService = likesService;
    }

    @GetMapping("/{videoId}/likes")
    public Map<String, Object> getLikes(@PathVariable String videoId) {
        long likes = likesService.getLikes(videoId);
        return Map.of(
                "videoId", videoId,
                "likes", likes);
    }

    @PostMapping("/{videoId}/like")
    public Map<String, Object> like(@PathVariable String videoId) {
        long likes = likesService.incrementLikes(videoId);
        return Map.of(
                "videoId", videoId,
                "likes", likes);
    }
}
