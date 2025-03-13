package com.pawan.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LUA_SCRIPT =
            "local count = redis.call('incr', KEYS[1]) " +
                    "if count == 1 or redis.call('ttl', KEYS[1]) == -1 then " + // Always reset TTL if missing
                    "    redis.call('expire', KEYS[1], tonumber(ARGV[1])) " +
                    "end " +
                    "return count";

    // Role-based request limits
    private static final Map<String, Integer> ROLE_LIMITS = Map.of(
            "FREE", 10,
            "PREMIUM", 15
    );

    public boolean isAllowed(String userId, String userRole) {
        if (userRole == null || !ROLE_LIMITS.containsKey(userRole)) {
            userRole = "FREE"; // Default role
        }

        int requestLimit = ROLE_LIMITS.get(userRole);
//        System.out.println("\nRequest Limit : " + requestLimit + "\n");

        // Use both userId and role in key to prevent conflicts
        String key = "rate_limit:" + userId + ":" + userRole;
//        System.out.println("\nKey : " + key + "\n");

        // Run Lua script to get the request count
        Long requestCount = redisTemplate.execute(
                new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                List.of(key),
                60 // Expiry time (seconds)
        );

//        System.out.println("\nRequestCount : " + requestCount + "\n");
        return requestCount != null && requestCount <= requestLimit;
    }
}
