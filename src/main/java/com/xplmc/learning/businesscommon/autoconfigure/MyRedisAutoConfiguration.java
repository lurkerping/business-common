package com.xplmc.learning.businesscommon.autoconfigure;

import com.xplmc.learning.businesscommon.redis.ClusterRedisOperations;
import com.xplmc.learning.businesscommon.redis.JedisRedisOperations;
import com.xplmc.learning.businesscommon.redis.RedisOperations;
import com.xplmc.learning.businesscommon.redis.RedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * custom redis auto configuration class
 * support jedis or cluster mode
 * cluster mode goes first
 *
 * @author luke
 */
@Configuration
@ConditionalOnClass({RedisOperations.class})
@EnableConfigurationProperties(RedisProperties.class)
public class MyRedisAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MyRedisAutoConfiguration.class);

    /**
     * redis configuration properties
     */
    private RedisProperties redisProperties;

    /**
     * MyRedisAutoConfiguration constructor
     *
     * @param redisProperties
     */
    public MyRedisAutoConfiguration(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * auto config RedisOperations
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RedisOperations.class)
    public RedisOperations redisOperations() {
        // prefer to user jedis cluster
        JedisCluster jedisCluster = createJedisCluster();
        if (jedisCluster != null) {
            logger.info("using JedisCluster, auto config ClusterRedisOperations");
            return new ClusterRedisOperations(jedisCluster);
        } else {
            logger.info("using JedisPool, auto config JedisRedisOperations");
            return new JedisRedisOperations(createJedisPool());
        }
    }

    /**
     * create jedis cluster instance using RedisProperties
     *
     * @return JedisCluster, null if not configured
     */
    private JedisCluster createJedisCluster() {
        RedisProperties.Cluster cluster = redisProperties.getCluster();
        if (cluster != null && !CollectionUtils.isEmpty(cluster.getNodes())) {
            //create jedis cluster
            JedisCluster jedisCluster = new JedisCluster(cluster.getNodes(),
                    redisProperties.getTimeout(), redisProperties.getSoTimeout(),
                    cluster.getMaxRedirects(), createJedisPoolConfig());
            return jedisCluster;
        }
        return null;
    }

    /**
     * create jedis instance using RedisProperties
     *
     * @return Jedis, null if not configured
     */
    private JedisPool createJedisPool() {
        RedisProperties.Cluster cluster = redisProperties.getCluster();
        return new JedisPool(createJedisPoolConfig(), redisProperties.getHost(), redisProperties.getPort(),
                redisProperties.getTimeout(), redisProperties.getPassword());
    }

    /**
     * create JedisPoolConfig object
     *
     * @return
     */
    private JedisPoolConfig createJedisPoolConfig() {
        //connection pool config
        RedisProperties.Pool pool = redisProperties.getPool();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(pool.getMinIdle());
        jedisPoolConfig.setMaxIdle(pool.getMaxIdle());
        jedisPoolConfig.setMaxTotal(pool.getMaxActive());
        return jedisPoolConfig;
    }

}
