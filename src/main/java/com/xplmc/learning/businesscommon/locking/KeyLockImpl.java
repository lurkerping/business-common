package com.xplmc.learning.businesscommon.locking;

import java.util.concurrent.locks.Lock;

/**
 * Implements the KeyLock
 */
public class KeyLockImpl implements KeyLock {

    private final KeyLockContext context;

    private final Lock lock;

    public KeyLockContext getContext() {
        return context;
    }

    public Lock getLock() {
        return lock;
    }

    public KeyLockImpl(KeyLockContext context, Lock lock) {
        this.context = context;
        this.lock = lock;
    }

    @Override
    public void release() {
        lock.unlock();
        context.cleanUp();
    }

    @Override
    public String toString() {
        return "KeyLockImpl{" +
                "context=" + context +
                ", lock=" + lock +
                '}';
    }

}
