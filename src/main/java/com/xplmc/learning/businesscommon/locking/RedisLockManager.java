package com.xplmc.learning.businesscommon.locking;

import com.google.common.collect.Lists;
import com.xplmc.learning.businesscommon.redis.RedisConstants;
import com.xplmc.learning.businesscommon.redis.RedisOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * use redis as a distributed lock
 *
 * @author luke
 */
public class RedisLockManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisLockManager.class);

    /**
     * unlock eval success return value, 1
     */
    private static final String UNLOCK_EVAL_SUCC_RETURN = "1";

    /**
     * redis operation object
     */
    private RedisOperations redisOperations;

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

    public RedisLockManager(RedisOperations redisOperations, String key, long expires) {
        this.redisOperations = redisOperations;
        this.key = key;
        this.expires = expires;
    }

    public RedisLockManager(RedisOperations redisOperations, String key) {
        this.redisOperations = redisOperations;
        this.key = key;
    }

    /**
     * try to acquire the lock, if the lock is hold by another request, return false immediately
     */
    public boolean tryLock() {
        try {
            String result = redisOperations.set(key, requestId, RedisConstants.SET_NOT_EXISTS,
                    RedisConstants.EXPIRE_TIME_IN_MILLIS, expires);
            return RedisConstants.SIMPLE_STRING_REPLAY.equals(result);
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
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        List<String> keys = Lists.newArrayList(key);
        List<String> args = Lists.newArrayList(requestId);
        Object result = redisOperations.eval(script, keys, args);
        if (result != null && UNLOCK_EVAL_SUCC_RETURN.equalsIgnoreCase(result.toString())) {
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
