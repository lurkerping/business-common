package com.xplmc.learning.businesscommon.redis;

/**
 * some redis constants
 *
 * @author luke
 */
public final class RedisConstants {

    /**
     * Only set the key if it does not already exist
     */
    public static final String SET_NOT_EXISTS = "NX";

    /**
     * Set the specified expire time, in milliseconds
     */
    public static final String EXPIRE_TIME_IN_MILLIS = "PX";

    /**
     * Set the specified expire time, in seconds
     */
    public static final String EXPIRE_TIME_IN_SECONDS = "EX";

    /**
     * RESP Simple Strings
     */
    public static final String SIMPLE_STRING_REPLAY = "OK";

}
