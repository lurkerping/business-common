package com.xplmc.learning.businesscommon.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * represent a readWriteLock context in the ConcurrentHashMap
 */
public class LocalKeyLockContext {

    private static final Logger logger = LoggerFactory.getLogger(LocalKeyLockContext.class);

    /**
     * the key string need to readWriteLock on
     */
    private final String key;

    /**
     * ReentrantReadWriteLock used to do the real locking work
     */
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

    /**
     * count how many times this readWriteLock has been acquired in the same threads
     */
    private AtomicInteger count = new AtomicInteger(0);

    /**
     * available flag
     */
    private AtomicBoolean available = new AtomicBoolean(true);

    /**
     * lock object used when new and release LocalKeyLockImpl
     */
    private final Object obj = new Object();


    public LocalKeyLockContext(String key) {
        this.key = key;
    }

    /**
     * create a new read readWriteLock
     */
    public LocalKeyLockImpl newReadLock() {
        Lock l = readWriteLock.readLock();
        return getKeyLockImpl(l);
    }

    /**
     * create a new write readWriteLock
     */
    public LocalKeyLockImpl newWriteLock() {
        Lock l = readWriteLock.writeLock();
        return getKeyLockImpl(l);
    }

    /**
     * maybe null when this context just released
     */
    private LocalKeyLockImpl getKeyLockImpl(Lock lock) {
        int nowCount = count.incrementAndGet();
        synchronized (obj) {
            if (available.get()) {
                logger.debug("getKeyLockImpl, LocalKeyLockContext: {}, current count: {}", this, nowCount);
                return new LocalKeyLockImpl(this, lock);
            } else {
                logger.info("LocalKeyLockContext not available, try again, {}", this);
                return null;
            }
        }
    }

    public void cleanUp() {
        int nowCount = count.decrementAndGet();
        logger.debug("cleanUp, LocalKeyLockContext: {}, current count: {}", this, nowCount);
        if (count.get() <= 0 && available.get()) {
            synchronized (obj) {
                available.set(false);
                LocalKeyLockContext removedContext = LocalKeyLockManager.remove(key);
                if (removedContext == this) {
                    logger.info("LocalKeyLockContext released: {}", this);
                } else {
                    logger.error("what's are you doing?");
                }
            }
        }
    }

    @Override
    public String toString() {
        return "LocalKeyLockContext{" +
                "key='" + key + '\'' +
                '}';
    }
}
