# Business Common
[![Build Status](https://travis-ci.org/lurkerping/business-common.svg?branch=master)](https://travis-ci.org/lurkerping/business-common)

provides some nice feature for real world project.

## Features
###Locks
#### String Key Based LocalLock
using `ConcurrentHashMap`'s `putIfAbsent` for local lock
```java
// 1. create a LocalKeyLockManager using your unique key string
LocalKeyLockManager localKeyLockManager = new LocalKeyLockManager("your key string");
LocalKeyLockImpl keyLock = null;
try {
    // 2. acquire the lock
    keyLock = localKeyLockManager.getKeyLock(read, tryLock, timeout, TimeUnit.MILLISECONDS);

    // 3.do some work
    ...
} catch (InterruptedException e) {
    // 4.handle InterruptedException
} finally {
    // 5.release the lock when finished
    if (keyLock != null) {
        keyLock.release();
    }
}
```

#### Distributed Lock
using redis's `set(key, value, nxxx, expx, time)`
```java
// 1. create a RedisLockManager
boolean acquired = false;
RedisLockManager redisLockManager = new RedisLockManager(redisOperations, "your key string", 2000);
try {
    // 2. acquire the lock
    acquired = redisLockManager.lock(timeout);
    
    // 3.do some work
    ...
} catch (InterruptedException e) {
    // 4.handle InterruptedException
} finally {
    // 5.release the lock when finished
    if (acquired) {
        redisLockManager.unlock();
    }
}
```
