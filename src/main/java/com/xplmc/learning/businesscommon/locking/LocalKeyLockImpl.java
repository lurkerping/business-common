package com.xplmc.learning.businesscommon.locking;

import java.util.concurrent.locks.Lock;

/**
 * Implements the LocalKeyLock
 *
 * @author luke
 */
public class LocalKeyLockImpl implements LocalKeyLock {

    private final LocalKeyLockContext context;

    private final Lock lock;

    public LocalKeyLockContext getContext() {
        return context;
    }

    public Lock getLock() {
        return lock;
    }

    public LocalKeyLockImpl(LocalKeyLockContext context, Lock lock) {
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
        return "LocalKeyLockImpl{" +
                "context=" + context +
                ", lock=" + lock +
                '}';
    }

}
