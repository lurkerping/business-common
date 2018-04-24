package com.xplmc.learning.businesscommon.sample.controller;

import com.xplmc.learning.businesscommon.redis.RedisOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Jedis PoolConfig sample controller
 *
 * @author luke
 */
@RestController
@RequestMapping("/sample/pool/jedis")
public class JedisPoolConfigController {

    private static final Logger logger = LoggerFactory.getLogger(JedisPoolConfigController.class);

    @Autowired
    private RedisOperations redisOperations;

    @RequestMapping(value = "/get")
    public Map<String, String> get() {
        Map<String, String> result = new HashMap<>(16);
        String randomKey = String.valueOf(Math.random());
        result.put("key", randomKey);
        result.put("value", redisOperations.get(randomKey));
        return result;
    }

    @RequestMapping(value = "/set")
    public Map<String, String> set() {
        Map<String, String> result = new HashMap<>(16);
        String randomKey = String.valueOf(Math.random());
        String randomValue = String.valueOf(Math.random());
        redisOperations.set(randomKey, randomValue);
        result.put("key", randomKey);
        result.put("value", randomValue);
        return result;
    }

}
