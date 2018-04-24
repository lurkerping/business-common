package com.xplmc.learning.businesscommon.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * redis operation implement using jedis
 *
 * @author luke
 */
public class JedisRedisOperations implements RedisOperations {

    private JedisPool jedisPool;

    public JedisRedisOperations(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public String set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value);
        }
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value, nxxx, expx, time);
        }
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.eval(script, keys, args);
        }
    }

}
