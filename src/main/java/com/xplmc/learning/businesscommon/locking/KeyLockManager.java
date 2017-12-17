package com.xplmc.learning.businesscommon.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * client shoud use this class to lock and unlock
 */
public final class KeyLockManager {

    public static final Logger logger = LoggerFactory.getLogger(KeyLockManager.class);

    private static final ConcurrentHashMap<String, KeyLockContext> LOCKED_KEYS = new ConcurrentHashMap<>();

    private final String key;

    public KeyLockManager(String key) {
        this.key = key;
    }

    public KeyLockImpl getReadKeyLock(boolean readLock, boolean tryLock) throws IOException {
        return getKeyLock(readLock, tryLock, 0, TimeUnit.SECONDS);
    }

    /**
     * all kinds of lock
     */
    public KeyLockImpl getKeyLock(boolean readLock, boolean tryLock, long timeout, TimeUnit unit) throws IOException {

        KeyLockContext keyLockContext = null;
        KeyLockImpl keyLock = null;

        boolean success = false;
        while (keyLock == null) {
            keyLockContext = new KeyLockContext(key);
            KeyLockContext existingContext = LOCKED_KEYS.putIfAbsent(key, keyLockContext);
            //if there is an existing context, use it
            if (existingContext != null) {
                keyLockContext = existingContext;
            }

            if (readLock) {
                keyLock = keyLockContext.newReadLock();
            } else {
                keyLock = keyLockContext.newWriteLock();
            }
        }

        try {
            if (tryLock) {
                boolean lockResult = keyLock.getLock().tryLock(timeout, unit);
                if (lockResult) {
                    success = true;
                    return keyLock;
                } else {
                    throw new IOException("Timed out waiting for lock for row: " + key);
                }
            } else {
                keyLock.getLock().lock();
                success = true;
                return keyLock;
            }
        } catch (InterruptedException ie) {
            //TODO do we need to do something about it?
            logger.error("InterruptedException when get try lock, keyLock: {}", keyLock, ie);
            InterruptedIOException iie = new InterruptedIOException();
            iie.initCause(ie);
            Thread.currentThread().interrupt();
            throw iie;
        } finally {
            if (!success) {
                logger.info("failed to acquire the lock, context: {}", keyLockContext);
                keyLockContext.cleanUp();
            }
        }
    }

    public static KeyLockContext remove(String key) {
        return LOCKED_KEYS.remove(key);
    }

}
