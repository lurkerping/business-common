package com.xplmc.learning.businesscommon.redis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * test redis eval's performance
 *
 * @author luke
 */
public class RedisEvalPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisEvalPerformanceTest.class);

    static long LOOPS = 10000;

    static Map<String, String> KVS = new HashMap<>(16000);

    static {
        Random random = new Random();
        for (int i = 0; i < LOOPS; i++) {
            String k = String.valueOf(random.nextInt(Integer.MAX_VALUE));
            String v = String.valueOf(random.nextInt(Integer.MAX_VALUE));
            KVS.put(k, v);
        }
    }

    private StringRedisTemplate stringRedisTemplate;

    public RedisEvalPerformanceTest(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addLast(
                new ResourcePropertySource("classpath:application.properties"));
        ctx.scan("com.xplmc.learning.businesscommon");
        ctx.refresh();

        StringRedisTemplate stringRedisTemplate = ctx.getBean(StringRedisTemplate.class);
        RedisEvalPerformanceTest test = new RedisEvalPerformanceTest(stringRedisTemplate);
        test.test1();
        test.test2();

        KVS.clear();
        ctx.close();
    }

    /**
     * compare eval's performance
     */
    public void test1() {
        //initRedis first
        this.initRedis();

        logger.info("start test1!");
        long start = System.currentTimeMillis();
        for (Map.Entry<String, String> kv : KVS.entrySet()) {
            compareAndDelete(kv.getKey(), kv.getValue());
        }
        logger.info("test1 cost:{}", System.currentTimeMillis() - start);
    }

    public void test2() {
        //initRedis first
        this.initRedis();

        logger.info("start test2!");
        long start = System.currentTimeMillis();
        for (Map.Entry<String, String> kv : KVS.entrySet()) {
            compareAndDeleteUsingEval(kv.getKey(), kv.getValue());
        }
        logger.info("test2 cost:{}", System.currentTimeMillis() - start);
    }

    private void initRedis() {
        long start = System.currentTimeMillis();
        logger.info("start initRedis!");
        for (Map.Entry<String, String> kv : KVS.entrySet()) {
            stringRedisTemplate.opsForValue().set(kv.getKey(), kv.getValue());
        }
        logger.info("initRedis done, cost: {}", System.currentTimeMillis() - start);
    }

    /**
     * compare and delete
     *
     * @param key
     * @param expectedValue
     */
    private boolean compareAndDelete(String key, String expectedValue) {
        if (StringUtils.equals(stringRedisTemplate.opsForValue().get(key), expectedValue)) {
            Boolean delResult = stringRedisTemplate.delete(key);
            return delResult != null && delResult.booleanValue();
        } else {
            return false;
        }
    }

    /**
     * compare and delete using eval
     *
     * @param key
     * @param expectedValue
     * @return
     */
    private boolean compareAndDeleteUsingEval(String key, String expectedValue) {
        Boolean result = stringRedisTemplate.execute((RedisConnection connection) -> {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long evalResult = connection.eval(
                    script.getBytes(StandardCharsets.UTF_8),
                    ReturnType.INTEGER,
                    1,
                    key.getBytes(StandardCharsets.UTF_8),
                    expectedValue.getBytes(StandardCharsets.UTF_8));
            return evalResult != null && evalResult.intValue() == 1;
        });
        return result != null && result.booleanValue();
    }

}
