package com.roomstack.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {

    // Key = ip + ":" + sessionId, Value = username
    private final Map<String, String> sessionMap = new ConcurrentHashMap<>();

    // Store mapping
    public void storeUser(String username, String ip, String sessionId) {
        String key = ip + ":" + sessionId;
        sessionMap.put(key, username);
    }

    // Get username by IP + sessionId
    public String getUsernameByIpAndSession(String ip, String sessionId) {
        return sessionMap.get(ip + ":" + sessionId);
    }

    // Remove mapping
    public void removeUser(String ip, String sessionId) {
        sessionMap.remove(ip + ":" + sessionId);
    }
}
