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

    private final int thresholdWrites;

    private final ConcurrentHashMap<String, LongAdder> windowCounts = new ConcurrentHashMap<>();
    private final AtomicReference<List<HotKeySnapshot>> lastHot = new AtomicReference<>(List.of());
    private final AtomicReference<Map<String, Long>> lastBusyWindowWrites = new AtomicReference<>(Map.of());
    private final AtomicLong windowsClosed = new AtomicLong();
    private final AtomicLong lastBusySnapshotAtEpochMs = new AtomicLong(0L);

    public HotKeyTracker(@Value("${app.hotkey.threshold-writes:100}") int thresholdWrites) {
        this.thresholdWrites = thresholdWrites;
    }

    public void recordWrite(String videoId) {
        windowCounts.computeIfAbsent(videoId, k -> new LongAdder()).increment();
    }

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

        if (!tally.isEmpty()) {
            lastBusyWindowWrites.set(Map.copyOf(tally));
            lastHot.set(List.copyOf(hot));
            lastBusySnapshotAtEpochMs.set(System.currentTimeMillis());
        }

        long n = windowsClosed.incrementAndGet();
        log.info(
                "Hot-key window #{} closed (tally this close={}, threshold={}, snapshot updated={})",
                n,
                tally,
                thresholdWrites,
                !tally.isEmpty());
        if (!hot.isEmpty()) {
            log.warn("Hot keys this window: {}", hot);
        }
    }

    public List<HotKeySnapshot> getLastHotKeys() {
        return lastHot.get();
    }

    public Map<String, Long> getCurrentWindowWrites() {
        Map<String, Long> map = new LinkedHashMap<>();
        windowCounts.forEach((videoId, adder) -> {
            long s = adder.sum();
            if (s > 0) {
                map.put(videoId, s);
            }
        });
        return map;
    }

    public Map<String, Long> getLastBusyWindowWrites() {
        return lastBusyWindowWrites.get();
    }

    public long getWindowsClosed() {
        return windowsClosed.get();
    }

    public int getThresholdWrites() {
        return thresholdWrites;
    }

    public long getLastBusySnapshotAtEpochMs() {
        return lastBusySnapshotAtEpochMs.get();
    }
}
