package ru.mtuci.coursemanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {
    private final ConcurrentMap<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();
    
    @Value("${app.security.rate-limit.login-attempts:5}")
    private int maxAttempts;
    
    @Value("${app.security.rate-limit.lockout-time-minutes:15}")
    private int lockoutMinutes;
    
    public void loginFailed(String username, String ipAddress) {
        String key = username + "|" + ipAddress;
        LoginAttempt attempt = attemptsCache.getOrDefault(key, new LoginAttempt(username, ipAddress));
        attempt.attemptCount++;
        attempt.lastAttempt = LocalDateTime.now();
        attemptsCache.put(key, attempt);
    }
    
    public void loginSucceeded(String username, String ipAddress) {
        String key = username + "|" + ipAddress;
        attemptsCache.remove(key);
    }
    
    public boolean isBlocked(String username, String ipAddress) {
        String key = username + "|" + ipAddress;
        LoginAttempt attempt = attemptsCache.get(key);
        
        if (attempt == null) {
            return false;
        }
        
        if (attempt.attemptCount >= maxAttempts) {
            LocalDateTime lockoutUntil = attempt.lastAttempt.plusMinutes(lockoutMinutes);
            if (LocalDateTime.now().isBefore(lockoutUntil)) {
                return true;
            } else {
                attemptsCache.remove(key);
                return false;
            }
        }
        
        return false;
    }
    
    public int getRemainingLockoutTime(String username, String ipAddress) {
        String key = username + "|" + ipAddress;
        LoginAttempt attempt = attemptsCache.get(key);
        
        if (attempt != null && attempt.attemptCount >= maxAttempts) {
            LocalDateTime lockoutUntil = attempt.lastAttempt.plusMinutes(lockoutMinutes);
            long minutes = java.time.Duration.between(LocalDateTime.now(), lockoutUntil).toMinutes();
            return Math.max(0, (int) minutes);
        }
        
        return 0;
    }
    
    public int getRemainingAttempts(String username, String ipAddress) {
        String key = username + "|" + ipAddress;
        LoginAttempt attempt = attemptsCache.get(key);
        
        if (attempt == null) {
            return maxAttempts;
        }
        
        return Math.max(0, maxAttempts - attempt.attemptCount);
    }
    
    @Scheduled(fixedRate = 300000)
    public void cleanUpExpiredAttempts() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        attemptsCache.entrySet().removeIf(entry -> 
            entry.getValue().lastAttempt.isBefore(cutoffTime)
        );
    }
    
    private static class LoginAttempt {
        String username;
        String ipAddress;
        int attemptCount;
        LocalDateTime lastAttempt;
        
        LoginAttempt(String username, String ipAddress) {
            this.username = username;
            this.ipAddress = ipAddress;
            this.attemptCount = 0;
            this.lastAttempt = LocalDateTime.now();
        }
    }
}