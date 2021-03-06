package com.xplmc.learning.businesscommon.redis;

import redis.clients.jedis.JedisCluster;

import java.util.List;

/**
 * redis operation implement using jedis cluster
 *
 * @author luke
 */
public class ClusterRedisOperations implements RedisOperations {

    private JedisCluster jedisCluster;

    public ClusterRedisOperations(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public String set(String key, String value) {
        return jedisCluster.set(key, value);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        return jedisCluster.set(key, value, nxxx, expx, time);
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return jedisCluster.eval(script, keys, args);
    }

}
