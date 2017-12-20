package com.xplmc.learning.businesscommon.locking;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * units test for LocalKeyLockContext
 */
public class LocalLocalKeyLockContextTest {

    private static final Logger logger = LoggerFactory.getLogger(LocalLocalKeyLockContextTest.class);

    private void testLocking(String key, boolean read, String flag) {
        LocalKeyLockManager localKeyLockManager = new LocalKeyLockManager(key);
        LocalKeyLockImpl keyLock = null;
        try {
            long start = System.currentTimeMillis();
            keyLock = localKeyLockManager.getKeyLock(read, false, 10, TimeUnit.SECONDS);
            logger.info("lock acquired, key:{}, flag: {}, time costs: {}, lock type: {}", key, flag, System.currentTimeMillis() - start, read ? "read" : "write");
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

        List<String> keyList = ImmutableList.of("abc", "def", "111", "000", "520", "6666abc");
        final String key = "abc";
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            final String flag = String.valueOf(i);
            futures.add(executorService.submit(() ->
                    testLocking(keyList.get(r.nextInt(keyList.size())), Math.random() > 0.2, flag)
            ));
        }
        Iterator<Future<?>> iterator = futures.iterator();
        while (iterator.hasNext()) {
            iterator.next().get();
            iterator.remove();
        }
    }

}
