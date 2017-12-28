package com.xplmc.learning.businesscommon.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * use redis as a distributed lock
 *
 * @author luke
 */
public class RedisLockManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisLockManager.class);

    /**
     * redis template object
     */
    private RedisTemplate<String, String> redisTemplate;

    /**
     * redis key using for locking
     */
    private String key;

    /**
     * random string represent this request
     */
    private String requestId = UUID.randomUUID().toString();

    /**
     * redis lock default expires time in milliseconds
     */
    private long expires = 30000L;

    public RedisLockManager(RedisTemplate<String, String> redisTemplate, String key, long expires) {
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.expires = expires;
    }

    public RedisLockManager(RedisTemplate<String, String> redisTemplate, String key) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    /**
     * try to acquire the lock, if the lock is hold by another request, return false immediately
     */
    public boolean tryLock() {
        try {
            Boolean result = redisTemplate.execute((RedisConnection connection) -> connection.set(
                    key.getBytes(StandardCharsets.UTF_8),
                    requestId.getBytes(StandardCharsets.UTF_8),
                    Expiration.milliseconds(expires),
                    RedisStringCommands.SetOption.SET_IF_ABSENT)
            );
            return result != null && result;
        } catch (Exception e) {
            logger.error("error setNX, key={}", key, e);
            return false;
        }

    }

    /**
     * try to acquire the lock, if the lock is hold by another request, wait until the timeouts reached
     *
     * @param timeouts time to wait in milliseconds
     * @return true if acquire the lock successfully
     */
    public boolean lock(long timeouts) throws InterruptedException {
        //time to give up acquiring the lock
        long endTime = System.currentTimeMillis() + timeouts;
        while (System.currentTimeMillis() < endTime) {
            boolean result = tryLock();
            if (result) {
                return true;
            } else {
                Thread.sleep(100L);
            }
        }
        return false;
    }

    /**
     * unlock, delete the key in redis
     */
    public void unlock() {
        //make sure the one who delete the key are the one who acquires the key
        Boolean result = redisTemplate.execute((RedisConnection connection) -> {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long evalResult = connection.eval(
                    script.getBytes(StandardCharsets.UTF_8),
                    ReturnType.INTEGER,
                    1,
                    key.getBytes(StandardCharsets.UTF_8),
                    requestId.getBytes(StandardCharsets.UTF_8));
            return evalResult != null && evalResult.intValue() == 1;
        });
        if (result != null && result) {
            logger.info("redis key lock: {}, unlock successfully", key);
        } else {
            logger.info("redis key lock: {}, unlock failed", key);
        }
    }

    @Override
    public String toString() {
        return "RedisLockManager{" +
                "key='" + key + '\'' +
                ", requestId='" + requestId + '\'' +
                ", expires=" + expires +
                '}';
    }

}
