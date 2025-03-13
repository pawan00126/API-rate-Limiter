package com.pawan.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LUA_SCRIPT =
            "local count = redis.call('incr', KEYS[1]) "+
             "if count == 1 then "+
             "    redis.call('expire', KEYS[1], tonumber(ARGV[1])) "+
             "end "+
             "return count";

    private static final Map<String, Integer> ROLE_LIMITS = Map.of(
            "FREE", 5,
            "PREMIUM",10
    );

    public boolean isAllowed(String userId, String userRole){

        if (userRole == null) {
            userRole = "FREE"; // Default role
        }


        int requestLimit = ROLE_LIMITS.getOrDefault(userRole, 5);
        String key = "rate_limit:"+userId;

//        System.out.println("key : "+key);
        Long requestCount = redisTemplate.execute(
                new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                List.of(key),
                String.valueOf(60)
        );

        // Handle null case
        if (requestCount == null) {
            return false;
        }

        return requestCount<=requestLimit;
    }
}
