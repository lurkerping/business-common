package com.xplmc.learning.businesscommon.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * client should use this class to lock and unlock
 *
 * @author luke
 */
public final class LocalKeyLockManager {

    private static final Logger logger = LoggerFactory.getLogger(LocalKeyLockManager.class);

    private static final ConcurrentHashMap<String, LocalKeyLockContext> LOCKED_KEYS = new ConcurrentHashMap<>();

    private final String key;

    public LocalKeyLockManager(String key) {
        this.key = key;
    }

    public LocalKeyLockImpl getReadKeyLock(boolean readLock, boolean tryLock) throws InterruptedException {
        return getKeyLock(readLock, tryLock, 0, TimeUnit.SECONDS);
    }

    /**
     * all kinds of lock
     */
    public LocalKeyLockImpl getKeyLock(boolean readLock, boolean tryLock, long timeout, TimeUnit unit) throws InterruptedException {

        LocalKeyLockContext localKeyLockContext = null;
        LocalKeyLockImpl keyLock = null;

        boolean success = false;
        while (keyLock == null) {
            localKeyLockContext = new LocalKeyLockContext(key);
            LocalKeyLockContext existingContext = LOCKED_KEYS.putIfAbsent(key, localKeyLockContext);
            //if there is an existing context, use it
            if (existingContext != null) {
                localKeyLockContext = existingContext;
            }

            if (readLock) {
                keyLock = localKeyLockContext.newReadLock();
            } else {
                keyLock = localKeyLockContext.newWriteLock();
            }
        }

        try {
            if (tryLock) {
                boolean lockResult = keyLock.getLock().tryLock(timeout, unit);
                if (lockResult) {
                    success = true;
                    return keyLock;
                } else {
                    logger.info("Timed out waiting for lock for row: " + key);
                    return null;
                }
            } else {
                keyLock.getLock().lock();
                success = true;
                return keyLock;
            }
        } finally {
            if (!success) {
                logger.info("failed to acquire the lock, context: {}", localKeyLockContext);
                localKeyLockContext.cleanUp();
            }
        }
    }

    public static LocalKeyLockContext remove(String key) {
        return LOCKED_KEYS.remove(key);
    }

}
