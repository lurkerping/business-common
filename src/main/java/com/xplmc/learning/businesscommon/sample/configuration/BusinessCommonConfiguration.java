package com.xplmc.learning.businesscommon.sample.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

/**
 * configuration class for business-common
 *
 * @author luke
 */
@Configuration
public class BusinessCommonConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BusinessCommonConfiguration.class);

    @Autowired
    RedisClusterConfigurationProperties redisClusterProperties;

    @Autowired
    RedisStandaloneConfigurationProperties redisStandaloneProperties;

    @Bean
    public RedisConnectionFactory connectionFactory() {
        if (CollectionUtils.isEmpty(redisClusterProperties.getNodes())) {
            logger.info("using redis standalone configuration: {}:{}", redisStandaloneProperties.getHostName(),
                    redisStandaloneProperties.getPort());
            return new JedisConnectionFactory(new RedisStandaloneConfiguration(redisStandaloneProperties.getHostName(),
                    redisStandaloneProperties.getPort()));
        } else {
            logger.info("using redis cluster configuration: {}", redisClusterProperties.getNodes());
            return new JedisConnectionFactory(
                    new RedisClusterConfiguration(redisClusterProperties.getNodes()));
        }
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(connectionFactory());
    }

}
