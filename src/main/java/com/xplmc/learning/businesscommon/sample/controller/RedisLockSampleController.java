package com.xplmc.learning.businesscommon.sample.controller;

import com.xplmc.learning.businesscommon.locking.RedisLockManager;
import com.xplmc.learning.businesscommon.redis.RedisOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private RedisOperations redisOperations;

    @RequestMapping(value = "/lock")
    public Map<String, String> lock(String key, @RequestParam(name = "timeouts", defaultValue = "0") long timeouts) {
        Map<String, String> result = new HashMap<>(16);
        RedisLockManager redisLockManager = new RedisLockManager(redisOperations, key);
        boolean acquired = false;
        //if timeouts equals 0, using tryLock mode
        if (timeouts == 0) {
            acquired = redisLockManager.tryLock();
        } else {
            try {
                acquired = redisLockManager.lock(timeouts);
            } catch (InterruptedException e) {
                //do nothing
            }
        }

        if (acquired) {
            try {
                String msg = "redis key lock successfully acquired";
                result.put("code", "200");
                result.put("msg", msg);
                try {
                    int sleep = new Random().nextInt(1000);
                    //fake some heavy work
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
