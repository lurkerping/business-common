package com.xplmc.learning.businesscommon.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * start embedded-redis for unit test
 *
 * @author luke
 */
@Component
public class EmbeddedRedisStarter {

    public static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisStarter.class);

    /**
     * redis configuration properties
     */
    private RedisProperties redisProperties;

    /**
     * EmbededRedisStarter constructor
     *
     * @param redisProperties
     */
    public EmbeddedRedisStarter(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * embedded redis server
     */
    private RedisServer redisServer;

    @PostConstruct
    public void init() {
        try {
            redisServer = new RedisServer(redisProperties.getPort());
            redisServer.start();
            logger.info("attention ****** embedded redis server started, port: {}", redisProperties.getPort());
        } catch (Exception e) {
            logger.error("error starting embedded redis server", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            redisServer.stop();
            logger.info("attention ****** embedded redis server stopped!");
        } catch (InterruptedException e) {
            logger.error("error stopping embedded redis server", e);
        }
    }

}
