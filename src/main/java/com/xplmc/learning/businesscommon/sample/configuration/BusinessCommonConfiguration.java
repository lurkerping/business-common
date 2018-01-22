package com.xplmc.learning.businesscommon.sample.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * configuration class for business-common
 *
 * @author luke
 */
@Configuration
public class BusinessCommonConfiguration {

    @Autowired
    RedisClusterConfigurationProperties redisClusterProperties;

    @Bean
    public JedisCluster connectionFactory() {
        //connection pool config
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(redisClusterProperties.getMinIdle());
        jedisPoolConfig.setMaxIdle(redisClusterProperties.getMaxIdle());
        jedisPoolConfig.setMaxTotal(redisClusterProperties.getMaxTotal());

        JedisCluster jedisCluster;
        if (StringUtils.isBlank(redisClusterProperties.getAuth())) {
            //redis server without auth
            jedisCluster = new JedisCluster(redisClusterProperties.getNodes(),
                    redisClusterProperties.getConnectionTimeout(), redisClusterProperties.getSoTimeout(),
                    redisClusterProperties.getMaxAttempts(), jedisPoolConfig);
        } else {
            //redis server do have an auth
            jedisCluster = new JedisCluster(redisClusterProperties.getNodes(),
                    redisClusterProperties.getConnectionTimeout(), redisClusterProperties.getSoTimeout(),
                    redisClusterProperties.getMaxAttempts(), redisClusterProperties.getAuth(), jedisPoolConfig);
        }
        return jedisCluster;
    }

}
