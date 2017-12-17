package com.xplmc.learning.businesscommon.locking;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * units test for KeyLockContext
 */
public class KeyLockContextTest {

    private static final Logger logger = LoggerFactory.getLogger(KeyLockContextTest.class);

    private void testLocking(String key, boolean read, String flag) {
        KeyLockManager keyLockManager = new KeyLockManager(key);
        KeyLockImpl keyLock = null;
        try {
            long start = System.currentTimeMillis();
            keyLock = keyLockManager.getKeyLock(read, false, 10, TimeUnit.SECONDS);
            logger.info("key:{}, flag: {}, time costs: {}, lock type: {}", key, flag, System.currentTimeMillis() - start, read);
            //pretending doing some heavy work
            long sleep = (long) (1000 * Math.random());
            try {
                Thread.sleep(sleep);
                logger.info("key:{}, flag: {}, sleep: {}", key, flag, sleep);
            } catch (InterruptedException e) {
                //
            }
        } catch (IOException e) {
            logger.error("timout when acquiring the lock", e);
        } finally {
            if (keyLock != null) {
                keyLock.release();
            }
        }

    }

    @Test
    public void testNewReadLock() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            futures.add(executorService.submit(() -> logger.info("warn up")));
        }
        Iterator<Future<?>> iterator = futures.iterator();
        while (iterator.hasNext()) {
            iterator.next().get();
            iterator.remove();
        }
        logger.info("futures size:{}", futures.size());

        logger.info("---------------------------------------");

        final String key = "abc";
        for (int i = 0; i < 10; i++) {
            final String flag = String.valueOf(i);
            futures.add(executorService.submit(() ->
                    testLocking(key, Math.random() > 1, flag)
            ));
        }
        iterator = futures.iterator();
        while (iterator.hasNext()) {
            iterator.next().get();
            iterator.remove();
        }
    }

}
