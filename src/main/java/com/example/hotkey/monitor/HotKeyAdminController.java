package com.example.hotkey.monitor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class HotKeyAdminController {

    private final HotKeyTracker hotKeyTracker;

    public HotKeyAdminController(HotKeyTracker hotKeyTracker) {
        this.hotKeyTracker = hotKeyTracker;
    }

    @GetMapping("/hot-keys")
    public Map<String, Object> hotKeys() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("hotKeys", hotKeyTracker.getLastHotKeys());
        body.put("writesLastWindow", hotKeyTracker.getLastWindowWrites());
        body.put("windowsClosed", hotKeyTracker.getWindowsClosed());
        body.put("thresholdWrites", hotKeyTracker.getThresholdWrites());
        return body;
    }
}
