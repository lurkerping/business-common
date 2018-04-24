package com.xplmc.learning.businesscommon.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * redis configuration properties
 * copy from org.springframework.boot.autoconfigure.data.redis.RedisProperties
 */
@ConfigurationProperties(prefix = "my.spring.redis")
public class RedisProperties {

    private static final Logger logger = LoggerFactory.getLogger(RedisProperties.class);

    /**
     * Database index used by the connection factory.
     */
    private int database = 0;

    /**
     * Connection URL. Overrides host, port, and password. User is ignored. Example:
     * redis://user:password@example.com:6379
     */
    private String url;

    /**
     * Redis server host.
     */
    private String host = "localhost";

    /**
     * Login password of the redis server.
     */
    private String password;

    /**
     * Redis server port.
     */
    private int port = 6379;

    /**
     * Whether to enable SSL support.
     */
    private boolean ssl;

    /**
     * Connection timeout.
     */
    private int timeout = 2000;

    /**
     * Socket timeout.
     */
    private int soTimeout = 3000;

    private Cluster cluster;

    private Pool pool = new Pool();

    public int getDatabase() {
        return this.database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    /**
     * Pool properties.
     */
    public static class Pool {

        /**
         * Maximum number of "idle" connections in the pool. Use a negative value to
         * indicate an unlimited number of idle connections.
         */
        private int maxIdle = 8;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if it is positive.
         */
        private int minIdle = 0;

        /**
         * Maximum number of connections that can be allocated by the pool at a given
         * time. Use a negative value for no limit.
         */
        private int maxActive = 8;

        /**
         * Maximum amount of time a connection allocation should block before throwing an
         * exception when the pool is exhausted. Use a negative value to block
         * indefinitely.
         */
        private int maxWait = 2000;

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(int maxWait) {
            this.maxWait = maxWait;
        }

    }

    /**
     * Cluster properties.
     */
    public static class Cluster {

        /**
         * spring.redis.cluster.nodes[0] = 127.0.0.1:7379
         * spring.redis.cluster.nodes[1] = 127.0.0.1:7380
         * ...
         */
        private Set<HostAndPort> nodes;

        /**
         * Maximum number of redirects to follow when executing commands across the
         * cluster.
         */
        private int maxRedirects = 5;


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


        public Integer getMaxRedirects() {
            return this.maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }

    }

}
