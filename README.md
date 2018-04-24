#Business Common
provides some nice feature for real world project.

##Features
### LOCKS
1. key based LocalLock: using ConcurrentHashMap's putIfAbsent for local lock
2. Key based Distributed Lock: using redis's set(key, value, nxxx, expx, time)
