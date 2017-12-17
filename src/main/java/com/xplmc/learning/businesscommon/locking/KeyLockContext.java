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
public class KeyLockContext {

    public static final Logger logger = LoggerFactory.getLogger(KeyLockContext.class);

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
     * lock object used when new and release KeyLockImpl
     */
    private final Object obj = new Object();


    public KeyLockContext(String key) {
        this.key = key;
    }

    /**
     * create a new read readWriteLock
     */
    public KeyLockImpl newReadLock() {
        Lock l = readWriteLock.readLock();
        return getKeyLockImpl(l);
    }

    /**
     * create a new write readWriteLock
     */
    public KeyLockImpl newWriteLock() {
        Lock l = readWriteLock.writeLock();
        return getKeyLockImpl(l);
    }

    /**
     * maybe null when this context just released
     */
    public KeyLockImpl getKeyLockImpl(Lock lock) {
        int nowCount = count.incrementAndGet();
        logger.info("increment count: {}", nowCount);
        synchronized (obj) {
            if (available.get()) {
                return new KeyLockImpl(this, lock);
            } else {
                logger.info("KeyLockContext not available, try again, {}", this);
                return null;
            }
        }
    }

    public void cleanUp() {
        int nowCount = count.decrementAndGet();
        logger.info("decrement count: {}", nowCount);
        if (count.get() <= 0 && available.get()) {
            synchronized (obj) {
                available.set(false);
                KeyLockContext removedContext = KeyLockManager.remove(key);
                if (removedContext != null) {
                    logger.error("what's are you doing?");
                }
            }
        }
    }

    @Override
    public String toString() {
        return "KeyLockContext{" +
                "key='" + key + '\'' +
                ", readWriteLock=" + readWriteLock +
                ", count=" + count +
                '}';
    }
}
