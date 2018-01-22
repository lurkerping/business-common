package com.xplmc.learning.businesscommon.redis;

import java.util.List;

/**
 * redis operation interface shield the real redis client we're using
 *
 * @author luke
 */
public interface RedisOperation {

    /**
     * get key
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * set key value pair
     *
     * @param key
     * @param value
     * @return
     */
    String set(String key, String value);

    /**
     * set key value pair with a bunch of other parameters
     *
     * @param key
     * @param value
     * @param nxxx
     * @param expx
     * @param time
     * @return
     */
    String set(final String key, final String value, final String nxxx, final String expx,
               final long time);

    /**
     * eval with keys and args
     *
     * @param script
     * @param keys
     * @param args
     * @return
     */
    Object eval(String script, final List<String> keys, final List<String> args);

}
