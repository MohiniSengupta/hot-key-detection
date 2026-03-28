package com.example.hotkey.monitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HotKeyTracker {

    private static final Logger log = LoggerFactory.getLogger(HotKeyTracker.class);

    private final ConcurrentHashMap<String, LongAdder> windowCounts = new ConcurrentHashMap<>();
    private final AtomicReference<List<HotKeySnapshot>> lastHot = new AtomicReference<>(List.of());
    private final AtomicReference<Map<String, Long>> lastWindowWrites = new AtomicReference<>(Map.of());
    private final AtomicLong windowsClosed = new AtomicLong();

    @Value("${app.hotkey.threshold-writes:100}")
    private int thresholdWrites;

    public void recordWrite(String videoId) {
        windowCounts.computeIfAbsent(videoId, k -> new LongAdder()).increment();
    }

    // First tick after one full window so an early burst is not wiped by an immediate empty close.
    @Scheduled(
            fixedRateString = "${app.hotkey.window-ms:10000}",
            initialDelayString = "${app.hotkey.window-ms:10000}")
    public void endWindow() {
        Map<String, Long> tally = new LinkedHashMap<>();
        List<HotKeySnapshot> hot = new ArrayList<>();
        for (var entry : windowCounts.entrySet()) {
            long count = entry.getValue().sumThenReset();
            if (count > 0) {
                tally.put(entry.getKey(), count);
            }
            if (count >= thresholdWrites) {
                hot.add(new HotKeySnapshot(entry.getKey(), count));
            }
        }
        lastWindowWrites.set(Map.copyOf(tally));
        lastHot.set(List.copyOf(hot));
        long n = windowsClosed.incrementAndGet();
        log.info(
                "Hot-key window #{} closed: writesLastWindow={}, threshold={}",
                n,
                tally,
                thresholdWrites);
        if (!hot.isEmpty()) {
            log.warn("Hot keys this window: {}", hot);
        }
    }

    public List<HotKeySnapshot> getLastHotKeys() {
        return lastHot.get();
    }

    public Map<String, Long> getLastWindowWrites() {
        return lastWindowWrites.get();
    }

    public long getWindowsClosed() {
        return windowsClosed.get();
    }

    public int getThresholdWrites() {
        return thresholdWrites;
    }
}
