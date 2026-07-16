package com.skillstorm.awsdemos.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Short-lived in-memory holder for synthesized audio so the browser can fetch it by id. Demo-only, not for concurrent multi-user production use. */
@Component
public class AudioCache {

    private static final int MAX_ENTRIES = 20;

    private final Map<String, byte[]> entries = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public synchronized String store(byte[] audio) {
        String id = UUID.randomUUID().toString();
        entries.put(id, audio);
        return id;
    }

    public synchronized byte[] get(String id) {
        return entries.get(id);
    }
}
