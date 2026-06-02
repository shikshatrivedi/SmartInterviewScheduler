package com.shiksha.scheduler.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory login attempt tracker.
 * Locks an account for LOCK_DURATION_MINUTES after MAX_ATTEMPTS failed logins.
 * Uses IP address or username as the key.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS       = 5;
    private static final long LOCK_DURATION_MS  = 15 * 60 * 1000L; // 15 minutes

    private final Map<String, Integer>  attempts  = new ConcurrentHashMap<>();
    private final Map<String, Long>     lockTime  = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attempts.remove(key);
        lockTime.remove(key);
    }

    public void loginFailed(String key) {
        int count = attempts.getOrDefault(key, 0) + 1;
        attempts.put(key, count);
        if (count >= MAX_ATTEMPTS) {
            lockTime.put(key, System.currentTimeMillis());
        }
    }

    public boolean isBlocked(String key) {
        if (!lockTime.containsKey(key)) return false;
        long lockedAt = lockTime.get(key);
        if (System.currentTimeMillis() - lockedAt > LOCK_DURATION_MS) {
            // Lock expired — reset
            attempts.remove(key);
            lockTime.remove(key);
            return false;
        }
        return true;
    }

    public int getRemainingAttempts(String key) {
        return Math.max(0, MAX_ATTEMPTS - attempts.getOrDefault(key, 0));
    }
}
