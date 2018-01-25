package com.xplmc.learning.businesscommon.locking;

import com.google.common.base.MoreObjects;
import com.xplmc.learning.businesscommon.redis.RedisOperation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * units test for RedisLockManager
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisLockManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisLockManagerTest.class);

    /**
     * executor using for test
     */
    private static ExecutorService executorService;

    @BeforeClass
    public static void init() {
        executorService = Executors.newFixedThreadPool(8);
    }

    @Autowired
    private RedisOperation redisOperation;

    /**
     * testing redis distributed lock
     */
    @Test
    public void testLock() throws Exception {
        final String key1 = "1" + String.valueOf(Math.random());
        final String key2 = "2" + String.valueOf(Math.random());

        //test four thread acquiring the lock at the same time
        List<Future<LockingExecInfo>> futureList = new ArrayList<>();
        futureList.add(executorService.submit(() -> getLockingExecInfo(key1, 500, 80)));
        futureList.add(executorService.submit(() -> getLockingExecInfo(key1, 500, 80)));
        futureList.add(executorService.submit(() -> getLockingExecInfo(key1, 500, 80)));
        futureList.add(executorService.submit(() -> getLockingExecInfo(key2, 1000, 100)));
        for (Future<LockingExecInfo> future : futureList) {
            logger.info("LockingExecInfo: {}", future.get());
            Assert.assertTrue(future.get().acquired);
        }

        //test three thread acquiring the tryLock at the same time
        Future<LockingExecInfo> f1 = executorService.submit(() -> getLockingExecInfo(key1, 0, 80));
        Future<LockingExecInfo> f2 = executorService.submit(() -> getLockingExecInfo(key1, 0, 80));
        Future<LockingExecInfo> f3 = executorService.submit(() -> getLockingExecInfo(key1, 0, 80));
        logger.info("LockingExecInfo: f1 {}", f1.get());
        logger.info("LockingExecInfo: f2 {}", f2.get());
        logger.info("LockingExecInfo: f3 {}", f3.get());
        boolean b1 = f1.get().acquired;
        boolean b2 = f2.get().acquired;
        boolean b3 = f3.get().acquired;
        Assert.assertTrue((b1 && !b2 && !b3) || (!b1 && b2 && !b3) || (!b1 && !b2 && b3));

        //test expired
        Future<LockingExecInfo> f4 = executorService.submit(() -> getLockingExecInfo(key1, 0, 2100));
        Thread.sleep(10);
        //make sure f4 get the lock
        Future<LockingExecInfo> f5 = executorService.submit(() -> getLockingExecInfo(key1, 3000, 1));
        Future<LockingExecInfo> f6 = executorService.submit(() -> getLockingExecInfo(key1, 1100, 1));
        logger.info("LockingExecInfo: f4 {}", f4.get());
        logger.info("LockingExecInfo: f5 {}", f5.get());
        logger.info("LockingExecInfo: f6 {}", f6.get());
        Assert.assertTrue(f4.get().acquired);
        Assert.assertTrue(f5.get().acquired);
        Assert.assertFalse(f6.get().acquired);

    }

    /**
     * tool method using for testing
     */
    private LockingExecInfo getLockingExecInfo(String key, long timeout, long processTime) {
        LockingExecInfo lei = new LockingExecInfo();
        RedisLockManager redisLockManager = new RedisLockManager(redisOperation, key, 2000);
        long start = System.currentTimeMillis();
        try {
            if (timeout == 0) {
                lei.acquired = redisLockManager.tryLock();
            } else {
                lei.acquired = redisLockManager.lock(timeout);
            }
            lei.waitingTime = System.currentTimeMillis() - start;
            if (lei.acquired) {
                if (processTime > 0) {
                    Thread.sleep(processTime);
                }
            }
        } catch (InterruptedException e) {
            //do nothing
        } finally {
            if (lei.acquired) {
                redisLockManager.unlock();
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
