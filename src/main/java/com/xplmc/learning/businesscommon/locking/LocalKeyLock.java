package com.xplmc.learning.businesscommon.locking;

/**
 * fine-grained locking with application specific keys
 *
 * @author luke
 */
public interface LocalKeyLock {

    /**
     * release the LocalKeyLockContext
     */
    void release();

}
