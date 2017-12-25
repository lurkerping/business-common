package com.xplmc.learning.businesscommon.sample.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * redis standalone mode configuration
 *
 * @author luke
 */
@Component
@ConfigurationProperties(prefix = "spring.redis.standalone")
public class RedisStandaloneConfigurationProperties {

    /**
     * redis standalone mode hostName
     */
    String hostName = "127.0.0.1";

    /**
     * redis standalone mode port
     */
    int port = 6379;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

}
