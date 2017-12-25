package com.xplmc.learning.businesscommon.sample.controller;

import com.xplmc.learning.businesscommon.locking.RedisLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * distribution lock using redis sample controller
 *
 * @author luke
 */
@RestController
@RequestMapping("/sample/locking/redis")
public class RedisLockSampleController {

    private static final Logger logger = LoggerFactory.getLogger(RedisLockSampleController.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping(value = "/order")
    public Map<String, String> order(String cardNo, Long amount) {
        Random random = new Random();
        Map<String, String> result = new HashMap<>(16);
        RedisLockManager redisLockManager = new RedisLockManager(stringRedisTemplate, cardNo);
        boolean acquired = redisLockManager.tryLock();
        if (acquired) {
            try {
                String msg = "redis key lock successfully acquired";
                result.put("code", "200");
                result.put("msg", msg);
                try {
                    int sleep = random.nextInt(1000);
                    Thread.sleep(sleep);
                    result.put("sleep", String.valueOf(sleep));
                } catch (InterruptedException e) {
                    //do nothing
                }
                logger.info(msg);
            } finally {
                redisLockManager.unlock();
            }
        } else {
            String msg = "redis key lock is already holding by another thread";
            result.put("code", "600");
            result.put("msg", msg);
            logger.info(msg);
        }
        return result;
    }

}
