package com.xplmc.learning.businesscommon.locking;

import com.google.common.base.MoreObjects;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * units test for LocalKeyLockContext
 */
public class LocalLocalKeyLockContextTest {

    private static final Logger logger = LoggerFactory.getLogger(LocalLocalKeyLockContextTest.class);

    /**
     * executor using for test
     */
    private static ExecutorService executorService;

    @BeforeClass
    public static void init() {
        executorService = Executors.newFixedThreadPool(8);
    }


    /**
     * testing read lock
     */
    @Test
    public void testGetKeyLock_ReadLock() throws Exception {
        final String key1 = "r1" + String.valueOf(Math.random());
        final String key2 = "r2" + String.valueOf(Math.random());

        //test two thread acquiring the write lock at the same time
        Future<LockingExecInfo> wf1 = executorService.submit(() -> getLockingExecInfo(key1, false, false, 1000, 1000));
        Future<LockingExecInfo> wf2 = executorService.submit(() -> getLockingExecInfo(key1, false, false, 1000, 1000));
        logger.info("LockingExecInfo wf1: {}", wf1.get());
        logger.info("LockingExecInfo wr2: {}", wf2.get());
        Assert.assertTrue(wf1.get().acquired && wf2.get().acquired);
        Assert.assertTrue(wf1.get().waitingTime + wf2.get().waitingTime < 1020);

        //test two thread acquiring the write lock at the same time, but with different key
        Future<LockingExecInfo> wf3 = executorService.submit(() -> getLockingExecInfo(key1, false, false, 1000, 1000));
        Future<LockingExecInfo> wf4 = executorService.submit(() -> getLockingExecInfo(key2, false, false, 1000, 1000));
        logger.info("LockingExecInfo wf3: {}", wf3.get());
        logger.info("LockingExecInfo wr4: {}", wf4.get());
        Assert.assertTrue(wf3.get().acquired && wf4.get().acquired);
        Assert.assertTrue(wf3.get().waitingTime + wf4.get().waitingTime < 10);

        //test write try lock
        Future<LockingExecInfo> wf5 = executorService.submit(() -> getLockingExecInfo(key1, false, true, 1000, 1000));
        //make sure the before lock acquired successfully
        Thread.sleep(10);
        Future<LockingExecInfo> wf6 = executorService.submit(() -> getLockingExecInfo(key1, false, true, 500, 1000));
        Future<LockingExecInfo> wf7 = executorService.submit(() -> getLockingExecInfo(key1, false, true, 0, 1000));
        Future<LockingExecInfo> wf8 = executorService.submit(() -> getLockingExecInfo(key1, false, true, 3000, 1000));
        Future<LockingExecInfo> wf9 = executorService.submit(() -> getLockingExecInfo(key1, false, false, 0, 1000));
        logger.info("LockingExecInfo wf5: {}", wf5.get());
        logger.info("LockingExecInfo wr6: {}", wf6.get());
        logger.info("LockingExecInfo wr7: {}", wf7.get());
        logger.info("LockingExecInfo wr8: {}", wf8.get());
        logger.info("LockingExecInfo wr9: {}", wf9.get());
        Assert.assertTrue(wf5.get().acquired);
        Assert.assertTrue(wf5.get().waitingTime < 10);
        Assert.assertFalse(wf6.get().acquired);
        Assert.assertTrue(wf6.get().waitingTime >= 500);
        Assert.assertFalse(wf7.get().acquired);
        Assert.assertTrue(wf7.get().waitingTime < 10);
        Assert.assertTrue(wf8.get().acquired && wf9.get().acquired);
        Assert.assertTrue(wf8.get().waitingTime + wf9.get().waitingTime > 2900);
    }

    /**
     * testing write lock
     */
    @Test
    public void testGetKeyLock_WriteLock() throws Exception {
        final String key1 = "w1" + String.valueOf(Math.random());
        final String key2 = "w2" + String.valueOf(Math.random());

        //test four thread acquiring the read lock at the same time
        List<Future<LockingExecInfo>> futureList = new ArrayList<>();
        futureList.add(executorService.submit(() -> getLockingExecInfo(key1, true, true, 1000, 0)));
        futureList.add(executorService.submit(() -> getLockingExecInfo(key1, true, true, 1000, 0)));
        futureList.add(executorService.submit(() -> getLockingExecInfo(key1, true, true, 1000, 0)));
        futureList.add(executorService.submit(() -> getLockingExecInfo(key2, true, true, 1000, 0)));
        for (Future<LockingExecInfo> future : futureList) {
            logger.info("LockingExecInfo: {}", future.get());
            Assert.assertTrue(future.get().acquired);
        }

        //test read and write lock
        futureList.clear();
        executorService.submit(() -> getLockingExecInfo(key1, false, true, 1000, 1000));
        //make sure the before lock acquired successfully
        Thread.sleep(10);
        Future<LockingExecInfo> rf1 = executorService.submit(() -> getLockingExecInfo(key1, true, true, 1000, 0));
        Future<LockingExecInfo> rf2 = executorService.submit(() -> getLockingExecInfo(key2, true, true, 1000, 0));
        Future<LockingExecInfo> rf3 = executorService.submit(() -> getLockingExecInfo(key1, true, true, 200, 0));
        logger.info("LockingExecInfo rf1: {}", rf1.get());
        logger.info("LockingExecInfo rf2: {}", rf2.get());
        logger.info("LockingExecInfo rf3: {}", rf3.get());
        Assert.assertTrue(rf1.get().acquired);
        Assert.assertTrue(rf2.get().acquired);
        Assert.assertFalse(rf3.get().acquired);
        Assert.assertTrue(rf1.get().waitingTime >= 980);
        Assert.assertTrue(rf2.get().waitingTime < 10);
        Assert.assertTrue(rf3.get().waitingTime >= 180);
    }

    /**
     * tool method using for testing
     */
    private LockingExecInfo getLockingExecInfo(String key, boolean read, boolean tryLock, long timeout, long processTime) {
        LockingExecInfo lei = new LockingExecInfo();
        LocalKeyLockManager localKeyLockManager = new LocalKeyLockManager(key);
        LocalKeyLockImpl keyLock = null;
        try {
            long start = System.currentTimeMillis();
            keyLock = localKeyLockManager.getKeyLock(read, tryLock, timeout, TimeUnit.MILLISECONDS);
            lei.waitingTime = (System.currentTimeMillis() - start);
            lei.acquired = (keyLock != null);

            //pretending to do some heavy work
            if (processTime > 0) {
                Thread.sleep(processTime);
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted when acquiring the lock", e);
        } finally {
            if (keyLock != null) {
                keyLock.release();
            }
        }
        return lei;
    }

    /**
     * record locking info for testing
     */
    private class LockingExecInfo {

        /**
         * acquired or not
         */
        private boolean acquired;

        /**
         * acquired time
         */
        private long waitingTime;

        private LockingExecInfo() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("acquired", acquired)
                    .add("waitingTime", waitingTime)
                    .toString();
        }
    }

    @AfterClass
    public static void destory() {
        executorService.shutdown();
    }

}
