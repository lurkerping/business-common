package com.xplmc.learning.businesscommon.sample.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * redis cluster mode configuration
 *
 * @author luke
 */
@Component
@ConfigurationProperties(prefix = "spring.redis.cluster")
public class RedisClusterConfigurationProperties {

    private static final Logger logger = LoggerFactory.getLogger(RedisClusterConfigurationProperties.class);

    /**
     * spring.redis.cluster.nodes[0] = 127.0.0.1:7379
     * spring.redis.cluster.nodes[1] = 127.0.0.1:7380
     * ...
     */
    private Set<HostAndPort> nodes;

    /**
     * min idle connection
     */
    private int minIdle = 0;

    /**
     * max idle connection
     */
    private int maxIdle = 8;

    /**
     * max total connection
     */
    private int maxTotal = 8;

    /**
     * connection timeout in millis
     */
    private int connectionTimeout = 2000;

    /**
     * socket timeout in millis
     */
    private int soTimeout = 2000;

    /**
     * auth for redis
     */
    private String auth;

    /**
     * max Attempts
     */
    private int maxAttempts = 3;

    /**
     * Get initial collection of known cluster nodes in format {@code host:port}.
     *
     * @return
     */
    public Set<HostAndPort> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        Set<HostAndPort> set = new HashSet<>();
        for (String node : nodes) {
            String[] nodeTuple = node.split(":");
            if (nodeTuple.length != 2) {
                logger.warn("wrong spring.redis.cluster format, {}, ignore", node);
                continue;
            }
            set.add(new HostAndPort(nodeTuple[0], Integer.parseInt(nodeTuple[1])));
        }
        this.nodes = set;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
}
