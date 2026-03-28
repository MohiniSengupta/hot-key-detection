package com.example.hotkey.monitor;

import java.util.LinkedHashMap;
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

    /**
     * Hot-key status for learning. Fields:
     * <ul>
     *   <li>{@code hotKeys} — videos that had writes ≥ threshold in the last <em>busy</em> closed window
     *       (may be empty if that window had traffic but all below threshold).</li>
     *   <li>{@code writesCurrentWindow} — in-flight counts since the last close; non-empty right after a burst.</li>
     *   <li>{@code writesLastBusyWindow} — per-video counts from the last closed window that had any writes;
     *       a later <em>quiet</em> window does not clear this.</li>
     *   <li>{@code lastBusySnapshotAtEpochMs} — when {@code writesLastBusyWindow} / {@code hotKeys} were last
     *       updated (milliseconds since epoch); {@code 0} means no busy window yet.</li>
     *   <li>{@code windowsClosed} — how many window closes have run since startup.</li>
     *   <li>{@code thresholdWrites} — configured threshold.</li>
     * </ul>
     */
    @GetMapping("/hot-keys")
    public Map<String, Object> hotKeys() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("hotKeys", hotKeyTracker.getLastHotKeys());
        body.put("writesCurrentWindow", hotKeyTracker.getCurrentWindowWrites());
        body.put("writesLastBusyWindow", hotKeyTracker.getLastBusyWindowWrites());
        body.put("lastBusySnapshotAtEpochMs", hotKeyTracker.getLastBusySnapshotAtEpochMs());
        body.put("windowsClosed", hotKeyTracker.getWindowsClosed());
        body.put("thresholdWrites", hotKeyTracker.getThresholdWrites());
        return body;
    }
}
