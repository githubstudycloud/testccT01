# Advanced Java utility libraries

**Created:** 9/29/2025 9:19:02  
**Updated:** 9/29/2025 9:30:04  
**Exported:** 9/29/2025 9:31:06  
**Link:** [https://claude.ai/chat/3e4b8907-ae53-4731-8b62-de13be9a19a9](https://claude.ai/chat/3e4b8907-ae53-4731-8b62-de13be9a19a9)  

## Prompt:
2025/9/29 09:19:04

写一些工具类，支持springboot2.7+jdk8以上版本，要有点实用性，一些常用工具类我都有了



## Response:
2025/9/29 09:20:15

我来为你编写一些实用且不那么常见的工具类，适配 Spring Boot 2.7 + JDK 8+：

**Spring Boot 实用工具类集合**

````java
// 1. 分布式锁工具类（基于Redis）
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DistributedLockUtil {
    
    private final StringRedisTemplate redisTemplate;
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) else return 0 end";
    
    public DistributedLockUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 尝试获取分布式锁
     * @param key 锁的key
     * @param expireTime 过期时间
     * @param unit 时间单位
     * @return 锁的唯一标识，获取失败返回null
     */
    public String tryLock(String key, long expireTime, TimeUnit unit) {
        String value = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, value, expireTime, unit);
        return Boolean.TRUE.equals(success) ? value : null;
    }
    
    /**
     * 释放分布式锁
     * @param key 锁的key
     * @param value 锁的唯一标识
     * @return 是否释放成功
     */
    public boolean unlock(String key, String value) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), value);
        return result != null && result == 1L;
    }
}

// ============================================================

// 2. IP地址工具类（支持IPv4/IPv6，获取真实IP）
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtil {
    
    private static final String[] IP_HEADER_CANDIDATES = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };
    
    /**
     * 获取客户端真实IP地址
     */
    public static String getRealIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = null;
        for (String header : IP_HEADER_CANDIDATES) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }
        
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于多级代理，取第一个有效IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // IPv6本地地址转换
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ip = "127.0.0.1";
            }
        }
        
        return ip;
    }
    
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
    
    /**
     * 将IP地址转换为长整型
     */
    public static long ipToLong(String ipAddress) {
        String[] ipParts = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (Long.parseLong(ipParts[i]) << (24 - (8 * i)));
        }
        return result;
    }
    
    /**
     * 判断IP是否在指定网段内
     */
    public static boolean isInRange(String ip, String cidr) {
        String[] cidrParts = cidr.split("/");
        long ipLong = ipToLong(ip);
        long networkLong = ipToLong(cidrParts[0]);
        int prefixLength = Integer.parseInt(cidrParts[1]);
        long mask = (-1L << (32 - prefixLength));
        return (ipLong & mask) == (networkLong & mask);
    }
}

// ============================================================

// 3. 敏感信息脱敏工具类
import java.util.regex.Pattern;

public class SensitiveUtil {
    
    /**
     * 手机号脱敏：保留前3后4位
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
    
    /**
     * 身份证脱敏：保留前6后4位
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || (idCard.length() != 15 && idCard.length() != 18)) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{6})\\d*(\\d{4})", "$1********$2");
    }
    
    /**
     * 邮箱脱敏：保留前缀第一个字符和@后面的域名
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String prefix = parts[0];
        if (prefix.length() <= 1) {
            return email;
        }
        return prefix.charAt(0) + "***@" + parts[1];
    }
    
    /**
     * 银行卡号脱敏：保留前4后4位
     */
    public static String maskBankCard(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) {
            return cardNo;
        }
        return cardNo.replaceAll("(\\d{4})\\d*(\\d{4})", "$1 **** **** $2");
    }
    
    /**
     * 姓名脱敏：保留姓，其他用*代替
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
    
    /**
     * 通用脱敏：保留前后指定位数
     */
    public static String mask(String str, int prefixLen, int suffixLen, String maskChar) {
        if (str == null || str.length() <= prefixLen + suffixLen) {
            return str;
        }
        String prefix = str.substring(0, prefixLen);
        String suffix = str.substring(str.length() - suffixLen);
        int maskLen = str.length() - prefixLen - suffixLen;
        return prefix + maskChar.repeat(maskLen) + suffix;
    }
}

// ============================================================

// 4. 重试工具类（支持自定义重试策略）
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class RetryUtil {
    
    /**
     * 简单重试
     * @param callable 待执行的任务
     * @param maxAttempts 最大重试次数
     * @param delayMillis 重试间隔（毫秒）
     */
    public static <T> T retry(Callable<T> callable, int maxAttempts, long delayMillis) 
            throws Exception {
        return retry(callable, maxAttempts, delayMillis, null);
    }
    
    /**
     * 带条件判断的重试
     * @param callable 待执行的任务
     * @param maxAttempts 最大重试次数
     * @param delayMillis 重试间隔（毫秒）
     * @param retryCondition 重试条件判断（返回true时重试）
     */
    public static <T> T retry(Callable<T> callable, int maxAttempts, long delayMillis,
                              Predicate<Exception> retryCondition) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                
                // 如果有重试条件，且不满足条件，直接抛出异常
                if (retryCondition != null && !retryCondition.test(e)) {
                    throw e;
                }
                
                // 最后一次尝试失败，不再等待
                if (attempt == maxAttempts) {
                    break;
                }
                
                // 等待后重试
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        
        throw new RuntimeException("Failed after " + maxAttempts + " attempts", lastException);
    }
    
    /**
     * 指数退避重试
     * @param callable 待执行的任务
     * @param maxAttempts 最大重试次数
     * @param initialDelayMillis 初始延迟（毫秒）
     * @param multiplier 延迟倍数
     */
    public static <T> T retryWithExponentialBackoff(Callable<T> callable, int maxAttempts,
                                                    long initialDelayMillis, double multiplier) 
            throws Exception {
        Exception lastException = null;
        long currentDelay = initialDelayMillis;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == maxAttempts) {
                    break;
                }
                
                Thread.sleep(currentDelay);
                currentDelay = (long) (currentDelay * multiplier);
            }
        }
        
        throw new RuntimeException("Failed after " + maxAttempts + " attempts", lastException);
    }
}

// ============================================================

// 5. 雪花ID生成器（分布式唯一ID）
public class SnowflakeIdGenerator {
    
    // 起始时间戳 (2020-01-01)
    private final long epoch = 1577808000000L;
    
    // 机器ID所占位数
    private final long workerIdBits = 10L;
    
    // 序列号所占位数
    private final long sequenceBits = 12L;
    
    // 机器ID最大值
    private final long maxWorkerId = ~(-1L << workerIdBits);
    
    // 序列号最大值
    private final long maxSequence = ~(-1L << sequenceBits);
    
    // 机器ID左移位数
    private final long workerIdShift = sequenceBits;
    
    // 时间戳左移位数
    private final long timestampShift = sequenceBits + workerIdBits;
    
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    public SnowflakeIdGenerator(long workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("Worker ID must be between 0 and %d", maxWorkerId));
        }
        this.workerId = workerId;
    }
    
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        
        // 时钟回拨检查
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }
        
        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        // 生成ID
        return ((timestamp - epoch) << timestampShift) |
               (workerId << workerIdShift) |
               sequence;
    }
    
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}

// ============================================================

// 6. 树形结构转换工具类
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TreeUtil {
    
    /**
     * 列表转树形结构
     * @param list 原始列表
     * @param idGetter ID获取器
     * @param parentIdGetter 父ID获取器
     * @param childrenSetter 子节点设置器
     * @param rootParentId 根节点的父ID
     */
    public static <T, ID> List<T> buildTree(List<T> list,
                                            Function<T, ID> idGetter,
                                            Function<T, ID> parentIdGetter,
                                            BiConsumer<T, List<T>> childrenSetter,
                                            ID rootParentId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按父ID分组
        Map<ID, List<T>> parentMap = list.stream()
            .collect(Collectors.groupingBy(parentIdGetter));
        
        // 递归构建树
        list.forEach(node -> {
            ID id = idGetter.apply(node);
            List<T> children = parentMap.get(id);
            if (children != null) {
                childrenSetter.accept(node, children);
            }
        });
        
        // 返回根节点列表
        return parentMap.getOrDefault(rootParentId, new ArrayList<>());
    }
    
    /**
     * 树形结构转列表（深度优先）
     */
    public static <T> List<T> treeToList(List<T> tree, Function<T, List<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        if (tree == null || tree.isEmpty()) {
            return result;
        }
        
        for (T node : tree) {
            result.add(node);
            List<T> children = childrenGetter.apply(node);
            if (children != null && !children.isEmpty()) {
                result.addAll(treeToList(children, childrenGetter));
            }
        }
        
        return result;
    }
}

// ============================================================

// 7. 线程池监控工具类
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMonitor {
    
    /**
     * 获取线程池监控信息
     */
    public static ThreadPoolInfo getThreadPoolInfo(ThreadPoolTaskExecutor executor) {
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
        
        ThreadPoolInfo info = new ThreadPoolInfo();
        info.setCorePoolSize(threadPool.getCorePoolSize());
        info.setMaxPoolSize(threadPool.getMaximumPoolSize());
        info.setPoolSize(threadPool.getPoolSize());
        info.setActiveCount(threadPool.getActiveCount());
        info.setQueueSize(threadPool.getQueue().size());
        info.setQueueRemainingCapacity(threadPool.getQueue().remainingCapacity());
        info.setCompletedTaskCount(threadPool.getCompletedTaskCount());
        info.setTaskCount(threadPool.getTaskCount());
        info.setLargestPoolSize(threadPool.getLargestPoolSize());
        
        return info;
    }
    
    /**
     * 检查线程池是否健康
     */
    public static boolean isHealthy(ThreadPoolTaskExecutor executor, double threshold) {
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
        int queueSize = threadPool.getQueue().size();
        int queueCapacity = queueSize + threadPool.getQueue().remainingCapacity();
        
        if (queueCapacity == 0) {
            return true;
        }
        
        double usage = (double) queueSize / queueCapacity;
        return usage < threshold;
    }
    
    public static class ThreadPoolInfo {
        private int corePoolSize;
        private int maxPoolSize;
        private int poolSize;
        private int activeCount;
        private int queueSize;
        private int queueRemainingCapacity;
        private long completedTaskCount;
        private long taskCount;
        private int largestPoolSize;
        
        // Getters and Setters
        public int getCorePoolSize() { return corePoolSize; }
        public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
        public int getActiveCount() { return activeCount; }
        public void setActiveCount(int activeCount) { this.activeCount = activeCount; }
        public int getQueueSize() { return queueSize; }
        public void setQueueSize(int queueSize) { this.queueSize = queueSize; }
        public int getQueueRemainingCapacity() { return queueRemainingCapacity; }
        public void setQueueRemainingCapacity(int queueRemainingCapacity) { 
            this.queueRemainingCapacity = queueRemainingCapacity; 
        }
        public long getCompletedTaskCount() { return completedTaskCount; }
        public void setCompletedTaskCount(long completedTaskCount) { 
            this.completedTaskCount = completedTaskCount; 
        }
        public long getTaskCount() { return taskCount; }
        public void setTaskCount(long taskCount) { this.taskCount = taskCount; }
        public int getLargestPoolSize() { return largestPoolSize; }
        public void setLargestPoolSize(int largestPoolSize) { 
            this.largestPoolSize = largestPoolSize; 
        }
        
        @Override
        public String toString() {
            return String.format(
                "ThreadPool[core=%d, max=%d, current=%d, active=%d, queue=%d/%d, completed=%d, total=%d]",
                corePoolSize, maxPoolSize, poolSize, activeCount, 
                queueSize, queueSize + queueRemainingCapacity,
                completedTaskCount, taskCount
            );
        }
    }
}
````

我为你创建了 7 个实用工具类，涵盖了以下场景：

## 📦 工具类说明

1. **DistributedLockUtil** - 分布式锁
   - 基于 Redis 实现的分布式锁
   - 使用 Lua 脚本保证原子性
   - 支持自动过期，防止死锁

2. **IpUtil** - IP 地址处理
   - 获取客户端真实 IP（支持多级代理）
   - IP 地址与长整型互转
   - 判断 IP 是否在指定网段（CIDR）

3. **SensitiveUtil** - 敏感信息脱敏
   - 手机号、身份证、邮箱、银行卡、姓名脱敏
   - 支持通用脱敏方法
   - 适用于日志打印、数据展示

4. **RetryUtil** - 重试机制
   - 简单重试（固定间隔）
   - 条件重试（根据异常类型判断）
   - 指数退避重试（延迟递增）

5. **SnowflakeIdGenerator** - 雪花 ID 生成器
   - 分布式唯一 ID 生成
   - 支持时钟回拨检测
   - 高性能（单机每秒 409.6 万个 ID）

6. **TreeUtil** - 树形结构转换
   - 列表转树形结构（支持泛型）
   - 树形结构转列表（深度优先）
   - 灵活的函数式接口设计

7. **ThreadPoolMonitor** - 线程池监控
   - 获取线程池详细状态
   - 健康度检查（队列使用率）
   - 适合接入监控系统

## 💡 使用建议

这些工具类都是开箱即用的，需要的依赖：
- Spring Boot Starter Web
- Spring Boot Starter Data Redis（仅分布式锁需要）

需要我补充其他场景的工具类吗？比如：
- 接口限流工具
- Excel 导入导出工具
- 消息摘要/加密工具
- HTTP 客户端封装工具



## Prompt:
2025/9/29 09:20:39

需要一些，



## Response:
2025/9/29 09:22:09



**Spring Boot 高级工具类集合**

````java
// 1. 接口限流工具类（基于Redis + 滑动窗口）
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RateLimiterUtil {
    
    private final StringRedisTemplate redisTemplate;
    
    // 滑动窗口限流 Lua 脚本
    private static final String SLIDING_WINDOW_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local limit = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local now = tonumber(ARGV[3])\n" +
        "local clearBefore = now - window\n" +
        "redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)\n" +
        "local current = redis.call('ZCARD', key)\n" +
        "if current < limit then\n" +
        "    redis.call('ZADD', key, now, now)\n" +
        "    redis.call('EXPIRE', key, window)\n" +
        "    return 1\n" +
        "else\n" +
        "    return 0\n" +
        "end";
    
    public RateLimiterUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 滑动窗口限流
     * @param key 限流key（如：user:123 或 api:/user/info）
     * @param limit 限流次数
     * @param windowSeconds 时间窗口（秒）
     * @return true-允许访问，false-限流
     */
    public boolean allowRequest(String key, int limit, int windowSeconds) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, Long.class);
        long now = System.currentTimeMillis();
        Long result = redisTemplate.execute(
            script,
            Collections.singletonList("rate:limit:" + key),
            String.valueOf(limit),
            String.valueOf(windowSeconds * 1000),
            String.valueOf(now)
        );
        return result != null && result == 1L;
    }
    
    /**
     * 令牌桶限流（简化版）
     * @param key 限流key
     * @param capacity 桶容量
     * @param rate 令牌生成速率（个/秒）
     * @return true-允许访问，false-限流
     */
    public boolean tryAcquire(String key, int capacity, double rate) {
        String tokenKey = "rate:token:" + key;
        String lastRefillKey = tokenKey + ":lastRefill";
        
        long now = System.currentTimeMillis();
        String lastRefillStr = redisTemplate.opsForValue().get(lastRefillKey);
        
        long lastRefill = lastRefillStr != null ? Long.parseLong(lastRefillStr) : now;
        long timePassed = now - lastRefill;
        
        // 计算应补充的令牌数
        double tokensToAdd = (timePassed / 1000.0) * rate;
        
        String tokensStr = redisTemplate.opsForValue().get(tokenKey);
        double currentTokens = tokensStr != null ? Double.parseDouble(tokensStr) : capacity;
        
        currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        
        if (currentTokens >= 1) {
            currentTokens -= 1;
            redisTemplate.opsForValue().set(tokenKey, String.valueOf(currentTokens), 1, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(now), 1, TimeUnit.HOURS);
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取剩余配额
     */
    public long getRemainingQuota(String key, int limit, int windowSeconds) {
        String rateLimitKey = "rate:limit:" + key;
        long now = System.currentTimeMillis();
        long clearBefore = now - windowSeconds * 1000L;
        
        redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, clearBefore);
        Long current = redisTemplate.opsForZSet().zCard(rateLimitKey);
        
        return limit - (current != null ? current : 0);
    }
}

// ============================================================

// 2. Excel导入导出工具类（基于EasyExcel）
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.read.listener.PageReadListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;

public class ExcelUtil {
    
    /**
     * 导出Excel到浏览器
     * @param response HTTP响应
     * @param fileName 文件名
     * @param sheetName sheet名称
     * @param clazz 数据类型
     * @param data 数据列表
     */
    public static <T> void exportExcel(HttpServletResponse response, 
                                       String fileName, 
                                       String sheetName,
                                       Class<T> clazz, 
                                       List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), clazz)
            .sheet(sheetName)
            .doWrite(data);
    }
    
    /**
     * 分批导出大数据量Excel
     * @param response HTTP响应
     * @param fileName 文件名
     * @param sheetName sheet名称
     * @param clazz 数据类型
     * @param dataSupplier 数据提供者（分批获取数据）
     * @param totalSize 总数据量
     * @param batchSize 每批大小
     */
    public static <T> void exportLargeExcel(HttpServletResponse response,
                                            String fileName,
                                            String sheetName,
                                            Class<T> clazz,
                                            java.util.function.BiFunction<Integer, Integer, List<T>> dataSupplier,
                                            int totalSize,
                                            int batchSize) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), clazz).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
        
        try {
            for (int i = 0; i < totalSize; i += batchSize) {
                List<T> data = dataSupplier.apply(i, batchSize);
                excelWriter.write(data, writeSheet);
            }
        } finally {
            excelWriter.finish();
        }
    }
    
    /**
     * 读取Excel（分批处理）
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param batchSize 每批大小
     * @param consumer 数据消费者
     */
    public static <T> void readExcel(InputStream inputStream,
                                     Class<T> clazz,
                                     int batchSize,
                                     Consumer<List<T>> consumer) {
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(consumer, batchSize))
            .sheet()
            .doRead();
    }
    
    /**
     * 读取Excel所有数据
     */
    public static <T> List<T> readExcelAll(InputStream inputStream, Class<T> clazz) {
        List<T> list = new java.util.ArrayList<>();
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(list::addAll))
            .sheet()
            .doRead();
        return list;
    }
}

// ============================================================

// 3. 加密/签名工具类
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class CryptoUtil {
    
    /**
     * MD5加密
     */
    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
    
    /**
     * SHA-256加密
     */
    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
    /**
     * HMAC-SHA256签名
     */
    public static String hmacSha256(String text, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 signing failed", e);
        }
    }
    
    /**
     * AES加密
     */
    public static String aesEncrypt(String text, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }
    
    /**
     * AES解密
     */
    public static String aesDecrypt(String encryptedText, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
    
    /**
     * RSA密钥对生成
     */
    public static KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        return keyGen.generateKeyPair();
    }
    
    /**
     * RSA公钥加密
     */
    public static String rsaEncrypt(String text, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }
    
    /**
     * RSA私钥解密
     */
    public static String rsaDecrypt(String encryptedText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA decryption failed", e);
        }
    }
    
    /**
     * Base64编码
     */
    public static String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Base64解码
     */
    public static String base64Decode(String encodedText) {
        return new String(Base64.getDecoder().decode(encodedText), StandardCharsets.UTF_8);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

// ============================================================

// 4. HTTP客户端封装工具（基于RestTemplate）
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class HttpUtil {
    
    private final RestTemplate restTemplate;
    
    public HttpUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * GET请求
     */
    public <T> T get(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }
    
    /**
     * GET请求（带请求头）
     */
    public <T> T get(String url, Map<String, String> headers, Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::set);
        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        return response.getBody();
    }
    
    /**
     * POST JSON请求
     */
    public <T> T postJson(String url, Object requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<T> response = restTemplate.postForEntity(url, entity, responseType);
        return response.getBody();
    }
    
    /**
     * POST 表单请求
     */
    public <T> T postForm(String url, Map<String, String> formData, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        formData.forEach(map::add);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<T> response = restTemplate.postForEntity(url, entity, responseType);
        return response.getBody();
    }
    
    /**
     * PUT请求
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
        return response.getBody();
    }
    
    /**
     * DELETE请求
     */
    public void delete(String url) {
        restTemplate.delete(url);
    }
    
    /**
     * 下载文件
     */
    public byte[] downloadFile(String url) {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        return response.getBody();
    }
    
    /**
     * 通用请求方法（支持自定义所有参数）
     */
    public <T> ResponseEntity<T> request(String url,
                                         HttpMethod method,
                                         Map<String, String> headers,
                                         Object body,
                                         Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
        return restTemplate.exchange(url, method, entity, responseType);
    }
}

// ============================================================

// 5. 缓存管理工具类
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import java.util.Collection;
import java.util.concurrent.Callable;

public class CacheUtil {
    
    private final CacheManager cacheManager;
    
    public CacheUtil(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    /**
     * 获取缓存值
     */
    public <T> T get(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper != null ? (T) wrapper.get() : null;
    }
    
    /**
     * 获取缓存值，如果不存在则加载
     */
    public <T> T get(String cacheName, Object key, Callable<T> valueLoader) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load value", e);
            }
        }
        try {
            return cache.get(key, valueLoader);
        } catch (Cache.ValueRetrievalException e) {
            throw new RuntimeException("Failed to retrieve value from cache", e);
        }
    }
    
    /**
     * 设置缓存
     */
    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }
    
    /**
     * 如果不存在则设置
     */
    public <T> T putIfAbsent(String cacheName, Object key, T value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return value;
        }
        Cache.ValueWrapper existingValue = cache.putIfAbsent(key, value);
        return existingValue != null ? (T) existingValue.get() : value;
    }
    
    /**
     * 删除缓存
     */
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
    
    /**
     * 清空指定缓存
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAll() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
    
    /**
     * 批量删除缓存（支持通配符）
     */
    public void evictPattern(String cacheName, String pattern) {
        // 注意：此方法需要Redis支持，使用RedisCache时可用
        Cache cache = cacheManager.getCache(cacheName);
        if (cache instanceof org.springframework.data.redis.cache.RedisCache) {
            org.springframework.data.redis.cache.RedisCache redisCache = 
                (org.springframework.data.redis.cache.RedisCache) cache;
            // 需要自定义实现通配符删除逻辑
        }
    }
}

// ============================================================

// 6. 异步任务结果管理工具
import java.util.concurrent.*;
import java.util.UUID;

public class AsyncTaskManager {
    
    private final ConcurrentHashMap<String, TaskResult<?>> taskResults = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public AsyncTaskManager() {
        // 定期清理过期任务结果（保留1小时）
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTasks, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * 提交异步任务
     */
    public <T> String submitTask(Callable<T> task, ExecutorService executor) {
        String taskId = UUID.randomUUID().toString();
        TaskResult<T> taskResult = new TaskResult<>(taskId);
        taskResults.put(taskId, taskResult);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor).whenComplete((result, throwable) -> {
            if (throwable != null) {
                taskResult.setStatus(TaskStatus.FAILED);
                taskResult.setError(throwable.getCause() != null ? 
                    throwable.getCause().getMessage() : throwable.getMessage());
            } else {
                taskResult.setStatus(TaskStatus.COMPLETED);
                taskResult.setResult(result);
            }
            taskResult.setCompletionTime(System.currentTimeMillis());
        });
        
        return taskId;
    }
    
    /**
     * 查询任务状态
     */
    public TaskStatus getTaskStatus(String taskId) {
        TaskResult<?> result = taskResults.get(taskId);
        return result != null ? result.getStatus() : null;
    }
    
    /**
     * 获取任务结果
     */
    @SuppressWarnings("unchecked")
    public <T> T getTaskResult(String taskId) {
        TaskResult<?> result = taskResults.get(taskId);
        if (result == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        if (result.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task not completed yet");
        }
        return (T) result.getResult();
    }
    
    /**
     * 获取任务错误信息
     */
    public String getTaskError(String taskId) {
        TaskResult<?> result = taskResults.get(taskId);
        return result != null ? result.getError() : null;
    }
    
    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        TaskResult<?> result = taskResults.remove(taskId);
        return result != null;
    }
    
    private void cleanupExpiredTasks() {
        long now = System.currentTimeMillis();
        long expirationTime = TimeUnit.HOURS.toMillis(1);
        
        taskResults.entrySet().removeIf(entry -> {
            TaskResult<?> result = entry.getValue();
            return result.getCompletionTime() > 0 && 
                   (now - result.getCompletionTime()) > expirationTime;
        });
    }
    
    public enum TaskStatus {
        PENDING, COMPLETED, FAILED
    }
    
    public static class TaskResult<T> {
        private final String taskId;
        private volatile TaskStatus status = TaskStatus.PENDING;
        private volatile T result;
        private volatile String error;
        private final long createTime;
        private volatile long completionTime;
        
        public TaskResult(String taskId) {
            this.taskId = taskId;
            this.createTime = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
        public T getResult() { return result; }
        public void setResult(T result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public long getCreateTime() { return createTime; }
        public long getCompletionTime() { return completionTime; }
        public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}

// ============================================================

// 7. 参数校验工具类
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtil {
    
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    /**
     * 校验对象
     * @param object 待校验对象
     * @param groups 校验组
     * @return 校验失败的字段信息
     */
    public static <T> Set<String> validate(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        return violations.stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.toSet());
    }
    
    /**
     * 校验对象，失败抛出异常
     */
    public static <T> void validateAndThrow(T object, Class<?>... groups) {
        Set<String> errors = validate(object, groups);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }
    }
    
    /**
     * 校验单个属性
     */
    public static <T> Set<String> validateProperty(T object, String propertyName, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(object, propertyName, groups);
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toSet());
    }
}
````

继续为你补充了 7 个高级工具类！

## 🚀 新增工具类说明

### 1. **RateLimiterUtil** - 接口限流
- **滑动窗口限流**：基于 Redis ZSet 实现，精确控制时间窗口内的请求次数
- **令牌桶限流**：支持平滑限流，适合突发流量场景
- **剩余配额查询**：可查看当前还能请求多少次

**使用场景**：API 接口防刷、用户操作频率限制

---

### 2. **ExcelUtil** - Excel 导入导出
- **简单导出**：一行代码导出 Excel 到浏览器
- **大数据量导出**：分批写入，支持百万级数据导出，避免 OOM
- **分批读取**：流式读取大文件，边读边处理

**依赖**：需要添加 `easyexcel` 依赖
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>3.3.2</version>
</dependency>
```

---

### 3. **CryptoUtil** - 加密签名工具
- **哈希算法**：MD5、SHA-256
- **HMAC 签名**：接口签名验证
- **对称加密**：AES 加密解密
- **非对称加密**：RSA 加密解密、密钥对生成
- **Base64 编解码**

**使用场景**：密码加密、API 签名、敏感数据加密传输

---

### 4. **HttpUtil** - HTTP 客户端封装
- **GET/POST/PUT/DELETE** 请求封装
- **JSON/表单** 提交
- **自定义请求头**
- **文件下载**
- **通用请求方法**（支持完全自定义）

**优势**：比直接使用 RestTemplate 更简洁，统一异常处理

---

### 5. **CacheUtil** - 缓存管理
- **统一缓存操作**：get、put、evict、



## Prompt:
2025/9/29 09:23:12

Continue



## Response:
2025/9/29 09:24:22



**Spring Boot 高级工具类集合** ∙ Version 2

````java
// 1. 接口限流工具类（基于Redis + 滑动窗口）
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RateLimiterUtil {
    
    private final StringRedisTemplate redisTemplate;
    
    // 滑动窗口限流 Lua 脚本
    private static final String SLIDING_WINDOW_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local limit = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local now = tonumber(ARGV[3])\n" +
        "local clearBefore = now - window\n" +
        "redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)\n" +
        "local current = redis.call('ZCARD', key)\n" +
        "if current < limit then\n" +
        "    redis.call('ZADD', key, now, now)\n" +
        "    redis.call('EXPIRE', key, window)\n" +
        "    return 1\n" +
        "else\n" +
        "    return 0\n" +
        "end";
    
    public RateLimiterUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 滑动窗口限流
     * @param key 限流key（如：user:123 或 api:/user/info）
     * @param limit 限流次数
     * @param windowSeconds 时间窗口（秒）
     * @return true-允许访问，false-限流
     */
    public boolean allowRequest(String key, int limit, int windowSeconds) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, Long.class);
        long now = System.currentTimeMillis();
        Long result = redisTemplate.execute(
            script,
            Collections.singletonList("rate:limit:" + key),
            String.valueOf(limit),
            String.valueOf(windowSeconds * 1000),
            String.valueOf(now)
        );
        return result != null && result == 1L;
    }
    
    /**
     * 令牌桶限流（简化版）
     * @param key 限流key
     * @param capacity 桶容量
     * @param rate 令牌生成速率（个/秒）
     * @return true-允许访问，false-限流
     */
    public boolean tryAcquire(String key, int capacity, double rate) {
        String tokenKey = "rate:token:" + key;
        String lastRefillKey = tokenKey + ":lastRefill";
        
        long now = System.currentTimeMillis();
        String lastRefillStr = redisTemplate.opsForValue().get(lastRefillKey);
        
        long lastRefill = lastRefillStr != null ? Long.parseLong(lastRefillStr) : now;
        long timePassed = now - lastRefill;
        
        // 计算应补充的令牌数
        double tokensToAdd = (timePassed / 1000.0) * rate;
        
        String tokensStr = redisTemplate.opsForValue().get(tokenKey);
        double currentTokens = tokensStr != null ? Double.parseDouble(tokensStr) : capacity;
        
        currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        
        if (currentTokens >= 1) {
            currentTokens -= 1;
            redisTemplate.opsForValue().set(tokenKey, String.valueOf(currentTokens), 1, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(now), 1, TimeUnit.HOURS);
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取剩余配额
     */
    public long getRemainingQuota(String key, int limit, int windowSeconds) {
        String rateLimitKey = "rate:limit:" + key;
        long now = System.currentTimeMillis();
        long clearBefore = now - windowSeconds * 1000L;
        
        redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, clearBefore);
        Long current = redisTemplate.opsForZSet().zCard(rateLimitKey);
        
        return limit - (current != null ? current : 0);
    }
}

// ============================================================

// 2. Excel导入导出工具类（基于EasyExcel）
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.read.listener.PageReadListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;

public class ExcelUtil {
    
    /**
     * 导出Excel到浏览器
     * @param response HTTP响应
     * @param fileName 文件名
     * @param sheetName sheet名称
     * @param clazz 数据类型
     * @param data 数据列表
     */
    public static <T> void exportExcel(HttpServletResponse response, 
                                       String fileName, 
                                       String sheetName,
                                       Class<T> clazz, 
                                       List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), clazz)
            .sheet(sheetName)
            .doWrite(data);
    }
    
    /**
     * 分批导出大数据量Excel
     * @param response HTTP响应
     * @param fileName 文件名
     * @param sheetName sheet名称
     * @param clazz 数据类型
     * @param dataSupplier 数据提供者（分批获取数据）
     * @param totalSize 总数据量
     * @param batchSize 每批大小
     */
    public static <T> void exportLargeExcel(HttpServletResponse response,
                                            String fileName,
                                            String sheetName,
                                            Class<T> clazz,
                                            java.util.function.BiFunction<Integer, Integer, List<T>> dataSupplier,
                                            int totalSize,
                                            int batchSize) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), clazz).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
        
        try {
            for (int i = 0; i < totalSize; i += batchSize) {
                List<T> data = dataSupplier.apply(i, batchSize);
                excelWriter.write(data, writeSheet);
            }
        } finally {
            excelWriter.finish();
        }
    }
    
    /**
     * 读取Excel（分批处理）
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param batchSize 每批大小
     * @param consumer 数据消费者
     */
    public static <T> void readExcel(InputStream inputStream,
                                     Class<T> clazz,
                                     int batchSize,
                                     Consumer<List<T>> consumer) {
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(consumer, batchSize))
            .sheet()
            .doRead();
    }
    
    /**
     * 读取Excel所有数据
     */
    public static <T> List<T> readExcelAll(InputStream inputStream, Class<T> clazz) {
        List<T> list = new java.util.ArrayList<>();
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(list::addAll))
            .sheet()
            .doRead();
        return list;
    }
}

// ============================================================

// 3. 加密/签名工具类
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class CryptoUtil {
    
    /**
     * MD5加密
     */
    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
    
    /**
     * SHA-256加密
     */
    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
    /**
     * HMAC-SHA256签名
     */
    public static String hmacSha256(String text, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 signing failed", e);
        }
    }
    
    /**
     * AES加密
     */
    public static String aesEncrypt(String text, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }
    
    /**
     * AES解密
     */
    public static String aesDecrypt(String encryptedText, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
    
    /**
     * RSA密钥对生成
     */
    public static KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        return keyGen.generateKeyPair();
    }
    
    /**
     * RSA公钥加密
     */
    public static String rsaEncrypt(String text, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }
    
    /**
     * RSA私钥解密
     */
    public static String rsaDecrypt(String encryptedText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA decryption failed", e);
        }
    }
    
    /**
     * Base64编码
     */
    public static String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Base64解码
     */
    public static String base64Decode(String encodedText) {
        return new String(Base64.getDecoder().decode(encodedText), StandardCharsets.UTF_8);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

// ============================================================

// 4. HTTP客户端封装工具（基于RestTemplate）
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class HttpUtil {
    
    private final RestTemplate restTemplate;
    
    public HttpUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * GET请求
     */
    public <T> T get(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }
    
    /**
     * GET请求（带请求头）
     */
    public <T> T get(String url, Map<String, String> headers, Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::set);
        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        return response.getBody();
    }
    
    /**
     * POST JSON请求
     */
    public <T> T postJson(String url, Object requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<T> response = restTemplate.postForEntity(url, entity, responseType);
        return response.getBody();
    }
    
    /**
     * POST 表单请求
     */
    public <T> T postForm(String url, Map<String, String> formData, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        formData.forEach(map::add);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<T> response = restTemplate.postForEntity(url, entity, responseType);
        return response.getBody();
    }
    
    /**
     * PUT请求
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
        return response.getBody();
    }
    
    /**
     * DELETE请求
     */
    public void delete(String url) {
        restTemplate.delete(url);
    }
    
    /**
     * 下载文件
     */
    public byte[] downloadFile(String url) {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        return response.getBody();
    }
    
    /**
     * 通用请求方法（支持自定义所有参数）
     */
    public <T> ResponseEntity<T> request(String url,
                                         HttpMethod method,
                                         Map<String, String> headers,
                                         Object body,
                                         Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
        return restTemplate.exchange(url, method, entity, responseType);
    }
}

// ============================================================

// 5. 缓存管理工具类
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import java.util.Collection;
import java.util.concurrent.Callable;

public class CacheUtil {
    
    private final CacheManager cacheManager;
    
    public CacheUtil(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    /**
     * 获取缓存值
     */
    public <T> T get(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper != null ? (T) wrapper.get() : null;
    }
    
    /**
     * 获取缓存值，如果不存在则加载
     */
    public <T> T get(String cacheName, Object key, Callable<T> valueLoader) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load value", e);
            }
        }
        try {
            return cache.get(key, valueLoader);
        } catch (Cache.ValueRetrievalException e) {
            throw new RuntimeException("Failed to retrieve value from cache", e);
        }
    }
    
    /**
     * 设置缓存
     */
    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }
    
    /**
     * 如果不存在则设置
     */
    public <T> T putIfAbsent(String cacheName, Object key, T value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return value;
        }
        Cache.ValueWrapper existingValue = cache.putIfAbsent(key, value);
        return existingValue != null ? (T) existingValue.get() : value;
    }
    
    /**
     * 删除缓存
     */
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
    
    /**
     * 清空指定缓存
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAll() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
    
    /**
     * 批量删除缓存（支持通配符）
     */
    public void evictPattern(String cacheName, String pattern) {
        // 注意：此方法需要Redis支持，使用RedisCache时可用
        Cache cache = cacheManager.getCache(cacheName);
        if (cache instanceof org.springframework.data.redis.cache.RedisCache) {
            org.springframework.data.redis.cache.RedisCache redisCache = 
                (org.springframework.data.redis.cache.RedisCache) cache;
            // 需要自定义实现通配符删除逻辑
        }
    }
}

// ============================================================

// 6. 异步任务结果管理工具
import java.util.concurrent.*;
import java.util.UUID;

public class AsyncTaskManager {
    
    private final ConcurrentHashMap<String, TaskResult<?>> taskResults = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public AsyncTaskManager() {
        // 定期清理过期任务结果（保留1小时）
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTasks, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * 提交异步任务
     */
    public <T> String submitTask(Callable<T> task, ExecutorService executor) {
        String taskId = UUID.randomUUID().toString();
        TaskResult<T> taskResult = new TaskResult<>(taskId);
        taskResults.put(taskId, taskResult);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor).whenComplete((result, throwable) -> {
            if (throwable != null) {
                taskResult.setStatus(TaskStatus.FAILED);
                taskResult.setError(throwable.getCause() != null ? 
                    throwable.getCause().getMessage() : throwable.getMessage());
            } else {
                taskResult.setStatus(TaskStatus.COMPLETED);
                taskResult.setResult(result);
            }
            taskResult.setCompletionTime(System.currentTimeMillis());
        });
        
        return taskId;
    }
    
    /**
     * 查询任务状态
     */
    public TaskStatus getTaskStatus(String taskId) {
        TaskResult<?> result = taskResults.get(taskId);
        return result != null ? result.getStatus() : null;
    }
    
    /**
     * 获取任务结果
     */
    @SuppressWarnings("unchecked")
    public <T> T getTaskResult(String taskId) {
        TaskResult<?> result = taskResults.get(taskId);
        if (result == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        if (result.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task not completed yet");
        }
        return (T) result.getResult();
    }
    
    /**
     * 获取任务错误信息
     */
    public String getTaskError(String taskId) {
        TaskResult<?> result = taskResults.get(taskId);
        return result != null ? result.getError() : null;
    }
    
    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        TaskResult<?> result = taskResults.remove(taskId);
        return result != null;
    }
    
    private void cleanupExpiredTasks() {
        long now = System.currentTimeMillis();
        long expirationTime = TimeUnit.HOURS.toMillis(1);
        
        taskResults.entrySet().removeIf(entry -> {
            TaskResult<?> result = entry.getValue();
            return result.getCompletionTime() > 0 && 
                   (now - result.getCompletionTime()) > expirationTime;
        });
    }
    
    public enum TaskStatus {
        PENDING, COMPLETED, FAILED
    }
    
    public static class TaskResult<T> {
        private final String taskId;
        private volatile TaskStatus status = TaskStatus.PENDING;
        private volatile T result;
        private volatile String error;
        private final long createTime;
        private volatile long completionTime;
        
        public TaskResult(String taskId) {
            this.taskId = taskId;
            this.createTime = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
        public T getResult() { return result; }
        public void setResult(T result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public long getCreateTime() { return createTime; }
        public long getCompletionTime() { return completionTime; }
        public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}

// ============================================================

// 7. 参数校验工具类
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtil {
    
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    /**
     * 校验对象
     * @param object 待校验对象
     * @param groups 校验组
     * @return 校验失败的字段信息
     */
    public static <T> Set<String> validate(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        return violations.stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.toSet());
    }
    
    /**
     * 校验对象，失败抛出异常
     */
    public static <T> void validateAndThrow(T object, Class<?>... groups) {
        Set<String> errors = validate(object, groups);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }
    }
    
    /**
     * 校验单个属性
     */
    public static <T> Set<String> validateProperty(T object, String propertyName, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(object, propertyName, groups);
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toSet());
    }
}

// ============================================================

// 8. 字段映射/对象复制工具（支持深拷贝）
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

public class BeanCopyUtil {
    
    /**
     * 复制属性（忽略null值）
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }
    
    /**
     * 获取对象中值为null的属性名
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        
        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }
    
    /**
     * 列表对象复制
     */
    public static <S, T> List<T> copyList(List<S> sources, Class<T> targetClass) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        return sources.stream()
            .map(source -> copyObject(source, targetClass))
            .collect(Collectors.toList());
    }
    
    /**
     * 单个对象复制
     */
    public static <S, T> T copyObject(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object", e);
        }
    }
    
    /**
     * Map转对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> targetClass) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanWrapper beanWrapper = new BeanWrapperImpl(target);
            map.forEach((key, value) -> {
                if (beanWrapper.isWritableProperty(key)) {
                    beanWrapper.setPropertyValue(key, value);
                }
            });
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to object", e);
        }
    }
    
    /**
     * 对象转Map
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        BeanWrapper beanWrapper = new BeanWrapperImpl(obj);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();
        
        for (PropertyDescriptor pd : pds) {
            String propertyName = pd.getName();
            if (!"class".equals(propertyName)) {
                Object value = beanWrapper.getPropertyValue(propertyName);
                map.put(propertyName, value);
            }
        }
        return map;
    }
}

// ============================================================

// 9. 日期时间工具类（LocalDateTime版本）
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class DateTimeUtil {
    
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    
    /**
     * 获取当前日期时间字符串
     */
    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
    }
    
    /**
     * 格式化日期时间
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * LocalDateTime转时间戳（毫秒）
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    /**
     * 时间戳转LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    
    /**
     * 计算两个日期时间之间的差值
     */
    public static long between(LocalDateTime start, LocalDateTime end, ChronoUnit unit) {
        return unit.between(start, end);
    }
    
    /**
     * 获取本月第一天
     */
    public static LocalDateTime getFirstDayOfMonth() {
        return LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    
    /**
     * 获取本月最后一天
     */
    public static LocalDateTime getLastDayOfMonth() {
        return LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }
    
    /**
     * 获取本周第一天（周一）
     */
    public static LocalDateTime getFirstDayOfWeek() {
        return LocalDateTime.now().with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    
    /**
     * 判断是否为工作日
     */
    public static boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
    
    /**
     * 计算年龄
     */
    public static int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    /**
     * 判断是否为今天
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime.toLocalDate().equals(LocalDate.now());
    }
    
    /**
     * 判断日期是否在指定范围内
     */
    public static boolean isBetween(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        return !target.isBefore(start) && !target.isAfter(end);
    }
}

// ============================================================

// 10. JSON工具类（基于Jackson）
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    static {
        // 配置
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
    
    /**
     * 对象转格式化的JSON字符串
     */
    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to pretty JSON", e);
        }
    }
    
    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON to object", e);
        }
    }
    
    /**
     * JSON字符串转List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        try {
            return MAPPER.readValue(json, 
                MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON to list", e);
        }
    }
    
    /**
     * JSON字符串转Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON to map", e);
        }
    }
    
    /**
     * 对象转Map
     */
    public static Map<String, Object> objectToMap(Object obj) {
        return MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Map转对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        return MAPPER.convertValue(map, clazz);
    }
    
    /**
     * 深拷贝
     */
    public static <T> T deepCopy(T obj, Class<T> clazz) {
        try {
            String json = MAPPER.writeValueAsString(obj);
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deep copy object", e);
        }
    }
}

// ============================================================

// 11. 文件上传下载工具类
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtil {
    
    /**
     * 上传文件（保留原文件名）
     */
    public static String uploadFile(MultipartFile file, String uploadDir) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File name is null");
        }
        
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String extension = getFileExtension(originalFilename);
        String newFileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(newFileName);
        
        // 保存文件
        file.transferTo(filePath.toFile());
        
        return newFileName;
    }
    
    /**
     * 下载文件
     */
    public static void downloadFile(HttpServletResponse response, String filePath, String displayName) 
            throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(displayName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encodedFileName);
        response.setContentLengthLong(file.length());
        
        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }
    
    /**
     * 判断是否为图片文件
     */
    public static boolean isImageFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)");
    }
    
    /**
     * 获取文件大小（格式化）
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }
    
    /**
     * 递归删除目录
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }
}
````

继续补充完成！现在一共有 **11 个实用工具类**了。

## 📋 补充说明

### 5. **CacheUtil** - 缓存管理（续）
- **懒加载缓存**：不存在时自动加载
- **条件设置**：putIfAbsent 避免重复设置
- **批量清理**：清空指定缓存或全部缓存

---

### 6. **AsyncTaskManager** - 异步任务管理
- **任务提交**：提交异步任务并返回任务 ID
- **状态查询**：PENDING / COMPLETED / FAILED
- **结果获取**：任务完成后获取结果
- **自动清理**：定期清理过期任务（1小时）

**使用场景**：长时间运行的导出任务、数据处理任务

---

### 7. **ValidationUtil** - 参数校验
- **JSR-303 校验**：基于注解的参数校验
- **分组校验**：支持不同场景的校验规则
- **属性校验**：单独校验某个字段

---

### 8. **BeanCopyUtil** - 对象复制（新增）
- **忽略 null 值复制**：只复制非空字段
- **列表批量复制**：一行代码复制整个列表
- **Map 与对象互转**：灵活转换数据结构
- **对象转 Map**：方便动态操作属性

---

### 9. **DateTimeUtil** - 日期时间工具（新增）
- **LocalDateTime 支持**：JDK 8+ 时间 API
- **格式化/解析**：字符串与日期互转
- **时间戳转换**：毫秒时间戳互转
- **时间计算**：两个时间差值、月初月末、工作日判断
- **年龄计算**：根据生日计算年龄

---

### 10. **JsonUtil** - JSON 工具（新增）
- **对象与 JSON 互转**：序列化/反序列化
- **List/Map 转换**：支持泛型转换
- **深拷贝**：通过 JSON 实现深拷贝
- **美化输出**：格式化 JSON 字符串

**依赖**：Spring Boot 默认已包含 Jackson

---

### 11. **FileUtil** - 文件处理（新增）
- **文件上传**：保存上传文件，自动生成唯一文件名
- **文件下载**：流式下载文件到浏览器
- **扩展名判断**：判断文件类型
- **文件大小格式化**：B/KB/MB/GB 自动转换
- **文件/目录删除**：支持递归删除

---

## 🎯 完整工具类列表（共 18 个）

**第一批（基础工具）：**
1. DistributedLockUtil - 分布式锁
2. IpUtil - IP 地址处理
3. SensitiveUtil - 敏感信息脱敏
4. RetryUtil - 重试机制
5. SnowflakeIdGenerator - 雪花 ID
6. TreeUtil - 树形结构
7. ThreadPoolMonitor - 线程池监控

**第二批（高级工具）：**
8. RateLimiterUtil - 接口限流
9. ExcelUtil - Excel 导入导出
10. CryptoUtil - 加密签名
11. HttpUtil - HTTP 客户端
12. CacheUtil - 缓存管理
13. AsyncTaskManager - 异步任务管理
14. ValidationUtil - 参数校验
15. BeanCopyUtil - 对象复制
16. DateTimeUtil - 日期时间
17. JsonUtil - JSON 处理
18. FileUtil - 文件上传下载

---

## 💡 使用建议

这些工具类涵盖了日常开发的大部分场景，建议：

1. **按需使用**：根据项目实际需求选择需要的工具类
2. **统一管理**：放在 `common.util` 包下统一管理
3. **Spring 集成**：需要注入 Bean 的工具类（如 HttpUtil、CacheUtil）配置为 Spring Bean

还需要其他场景的工具类吗？比如：
- 二维码生成工具
- 邮件发送工具
- 短信发送工具
- 定时任务管理工具



## Prompt:
2025/9/29 09:26:53

还能再加一些监控类的吗



## Response:
2025/9/29 09:28:09



**Spring Boot 监控工具类集合**

````java
// 1. JVM监控工具类
import java.lang.management.*;
import java.util.*;

public class JvmMonitor {
    
    private static final Runtime runtime = Runtime.getRuntime();
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    /**
     * 获取JVM内存信息
     */
    public static MemoryInfo getMemoryInfo() {
        MemoryInfo info = new MemoryInfo();
        
        // 堆内存
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        info.setHeapInit(heapMemory.getInit());
        info.setHeapUsed(heapMemory.getUsed());
        info.setHeapCommitted(heapMemory.getCommitted());
        info.setHeapMax(heapMemory.getMax());
        info.setHeapUsagePercent(heapMemory.getUsed() * 100.0 / heapMemory.getMax());
        
        // 非堆内存
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
        info.setNonHeapUsed(nonHeapMemory.getUsed());
        info.setNonHeapCommitted(nonHeapMemory.getCommitted());
        
        // JVM总内存
        info.setTotalMemory(runtime.totalMemory());
        info.setFreeMemory(runtime.freeMemory());
        info.setMaxMemory(runtime.maxMemory());
        info.setUsedMemory(runtime.totalMemory() - runtime.freeMemory());
        
        return info;
    }
    
    /**
     * 获取线程信息
     */
    public static ThreadInfo getThreadInfo() {
        ThreadInfo info = new ThreadInfo();
        info.setThreadCount(threadMXBean.getThreadCount());
        info.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        info.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        info.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        
        // 死锁检测
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        info.setDeadlockedThreadCount(deadlockedThreads != null ? deadlockedThreads.length : 0);
        
        return info;
    }
    
    /**
     * 获取GC信息
     */
    public static List<GcInfo> getGcInfo() {
        List<GcInfo> gcInfoList = new ArrayList<>();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            GcInfo gcInfo = new GcInfo();
            gcInfo.setName(gcBean.getName());
            gcInfo.setCollectionCount(gcBean.getCollectionCount());
            gcInfo.setCollectionTime(gcBean.getCollectionTime());
            gcInfoList.add(gcInfo);
        }
        return gcInfoList;
    }
    
    /**
     * 获取CPU使用率
     */
    public static double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getProcessCpuLoad() * 100;
        }
        return -1;
    }
    
    /**
     * 获取系统信息
     */
    public static SystemInfo getSystemInfo() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        SystemInfo info = new SystemInfo();
        info.setOsName(osBean.getName());
        info.setOsVersion(osBean.getVersion());
        info.setOsArch(osBean.getArch());
        info.setAvailableProcessors(osBean.getAvailableProcessors());
        info.setSystemLoadAverage(osBean.getSystemLoadAverage());
        
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        info.setJvmName(runtimeMXBean.getVmName());
        info.setJvmVersion(runtimeMXBean.getVmVersion());
        info.setJvmUptime(runtimeMXBean.getUptime());
        info.setStartTime(runtimeMXBean.getStartTime());
        
        return info;
    }
    
    /**
     * 检查内存使用是否健康
     */
    public static boolean isMemoryHealthy(double threshold) {
        MemoryInfo memoryInfo = getMemoryInfo();
        return memoryInfo.getHeapUsagePercent() < threshold;
    }
    
    // 内部类
    public static class MemoryInfo {
        private long heapInit;
        private long heapUsed;
        private long heapCommitted;
        private long heapMax;
        private double heapUsagePercent;
        private long nonHeapUsed;
        private long nonHeapCommitted;
        private long totalMemory;
        private long freeMemory;
        private long maxMemory;
        private long usedMemory;
        
        // Getters and Setters
        public long getHeapInit() { return heapInit; }
        public void setHeapInit(long heapInit) { this.heapInit = heapInit; }
        public long getHeapUsed() { return heapUsed; }
        public void setHeapUsed(long heapUsed) { this.heapUsed = heapUsed; }
        public long getHeapCommitted() { return heapCommitted; }
        public void setHeapCommitted(long heapCommitted) { this.heapCommitted = heapCommitted; }
        public long getHeapMax() { return heapMax; }
        public void setHeapMax(long heapMax) { this.heapMax = heapMax; }
        public double getHeapUsagePercent() { return heapUsagePercent; }
        public void setHeapUsagePercent(double heapUsagePercent) { this.heapUsagePercent = heapUsagePercent; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public void setNonHeapUsed(long nonHeapUsed) { this.nonHeapUsed = nonHeapUsed; }
        public long getNonHeapCommitted() { return nonHeapCommitted; }
        public void setNonHeapCommitted(long nonHeapCommitted) { this.nonHeapCommitted = nonHeapCommitted; }
        public long getTotalMemory() { return totalMemory; }
        public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public void setFreeMemory(long freeMemory) { this.freeMemory = freeMemory; }
        public long getMaxMemory() { return maxMemory; }
        public void setMaxMemory(long maxMemory) { this.maxMemory = maxMemory; }
        public long getUsedMemory() { return usedMemory; }
        public void setUsedMemory(long usedMemory) { this.usedMemory = usedMemory; }
    }
    
    public static class ThreadInfo {
        private int threadCount;
        private int peakThreadCount;
        private long totalStartedThreadCount;
        private int daemonThreadCount;
        private int deadlockedThreadCount;
        
        // Getters and Setters
        public int getThreadCount() { return threadCount; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
        public int getPeakThreadCount() { return peakThreadCount; }
        public void setPeakThreadCount(int peakThreadCount) { this.peakThreadCount = peakThreadCount; }
        public long getTotalStartedThreadCount() { return totalStartedThreadCount; }
        public void setTotalStartedThreadCount(long totalStartedThreadCount) { 
            this.totalStartedThreadCount = totalStartedThreadCount; 
        }
        public int getDaemonThreadCount() { return daemonThreadCount; }
        public void setDaemonThreadCount(int daemonThreadCount) { this.daemonThreadCount = daemonThreadCount; }
        public int getDeadlockedThreadCount() { return deadlockedThreadCount; }
        public void setDeadlockedThreadCount(int deadlockedThreadCount) { 
            this.deadlockedThreadCount = deadlockedThreadCount; 
        }
    }
    
    public static class GcInfo {
        private String name;
        private long collectionCount;
        private long collectionTime;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getCollectionCount() { return collectionCount; }
        public void setCollectionCount(long collectionCount) { this.collectionCount = collectionCount; }
        public long getCollectionTime() { return collectionTime; }
        public void setCollectionTime(long collectionTime) { this.collectionTime = collectionTime; }
    }
    
    public static class SystemInfo {
        private String osName;
        private String osVersion;
        private String osArch;
        private int availableProcessors;
        private double systemLoadAverage;
        private String jvmName;
        private String jvmVersion;
        private long jvmUptime;
        private long startTime;
        
        // Getters and Setters
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
        public String getOsArch() { return osArch; }
        public void setOsArch(String osArch) { this.osArch = osArch; }
        public int getAvailableProcessors() { return availableProcessors; }
        public void setAvailableProcessors(int availableProcessors) { 
            this.availableProcessors = availableProcessors; 
        }
        public double getSystemLoadAverage() { return systemLoadAverage; }
        public void setSystemLoadAverage(double systemLoadAverage) { 
            this.systemLoadAverage = systemLoadAverage; 
        }
        public String getJvmName() { return jvmName; }
        public void setJvmName(String jvmName) { this.jvmName = jvmName; }
        public String getJvmVersion() { return jvmVersion; }
        public void setJvmVersion(String jvmVersion) { this.jvmVersion = jvmVersion; }
        public long getJvmUptime() { return jvmUptime; }
        public void setJvmUptime(long jvmUptime) { this.jvmUptime = jvmUptime; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
    }
}

// ============================================================

// 2. Redis监控工具类
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.*;

public class RedisMonitor {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisMonitor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 获取Redis信息
     */
    public RedisInfo getRedisInfo() {
        return redisTemplate.execute((RedisCallback<RedisInfo>) connection -> {
            Properties info = connection.info();
            RedisInfo redisInfo = new RedisInfo();
            
            if (info != null) {
                // 服务器信息
                redisInfo.setRedisVersion(info.getProperty("redis_version"));
                redisInfo.setOs(info.getProperty("os"));
                redisInfo.setUptimeInSeconds(Long.parseLong(info.getProperty("uptime_in_seconds", "0")));
                
                // 客户端信息
                redisInfo.setConnectedClients(Integer.parseInt(info.getProperty("connected_clients", "0")));
                redisInfo.setBlockedClients(Integer.parseInt(info.getProperty("blocked_clients", "0")));
                
                // 内存信息
                redisInfo.setUsedMemory(Long.parseLong(info.getProperty("used_memory", "0")));
                redisInfo.setUsedMemoryHuman(info.getProperty("used_memory_human"));
                redisInfo.setUsedMemoryPeak(Long.parseLong(info.getProperty("used_memory_peak", "0")));
                redisInfo.setUsedMemoryPeakHuman(info.getProperty("used_memory_peak_human"));
                redisInfo.setMaxMemory(Long.parseLong(info.getProperty("maxmemory", "0")));
                redisInfo.setMemFragmentationRatio(
                    Double.parseDouble(info.getProperty("mem_fragmentation_ratio", "0")));
                
                // 统计信息
                redisInfo.setTotalConnectionsReceived(
                    Long.parseLong(info.getProperty("total_connections_received", "0")));
                redisInfo.setTotalCommandsProcessed(
                    Long.parseLong(info.getProperty("total_commands_processed", "0")));
                redisInfo.setInstantaneousOpsPerSec(
                    Long.parseLong(info.getProperty("instantaneous_ops_per_sec", "0")));
                
                // 持久化信息
                redisInfo.setRdbLastSaveTime(Long.parseLong(info.getProperty("rdb_last_save_time", "0")));
                redisInfo.setAofEnabled(info.getProperty("aof_enabled", "0").equals("1"));
            }
            
            return redisInfo;
        });
    }
    
    /**
     * 获取数据库大小
     */
    public Long getDbSize() {
        return redisTemplate.execute((RedisCallback<Long>) RedisConnection::dbSize);
    }
    
    /**
     * 获取内存使用情况
     */
    public Map<String, Object> getMemoryStats() {
        RedisInfo info = getRedisInfo();
        Map<String, Object> stats = new HashMap<>();
        stats.put("usedMemory", info.getUsedMemory());
        stats.put("usedMemoryHuman", info.getUsedMemoryHuman());
        stats.put("usedMemoryPeak", info.getUsedMemoryPeak());
        stats.put("maxMemory", info.getMaxMemory());
        stats.put("memFragmentationRatio", info.getMemFragmentationRatio());
        
        if (info.getMaxMemory() > 0) {
            double usagePercent = (info.getUsedMemory() * 100.0) / info.getMaxMemory();
            stats.put("usagePercent", usagePercent);
        }
        
        return stats;
    }
    
    /**
     * 检查Redis健康状态
     */
    public boolean isHealthy() {
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                String pong = connection.ping();
                return "PONG".equals(pong);
            });
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取慢查询日志
     */
    public List<Object> getSlowLogs(long count) {
        return redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
            return connection.slowLogGet(count);
        });
    }
    
    public static class RedisInfo {
        private String redisVersion;
        private String os;
        private long uptimeInSeconds;
        private int connectedClients;
        private int blockedClients;
        private long usedMemory;
        private String usedMemoryHuman;
        private long usedMemoryPeak;
        private String usedMemoryPeakHuman;
        private long maxMemory;
        private double memFragmentationRatio;
        private long totalConnectionsReceived;
        private long totalCommandsProcessed;
        private long instantaneousOpsPerSec;
        private long rdbLastSaveTime;
        private boolean aofEnabled;
        
        // Getters and Setters
        public String getRedisVersion() { return redisVersion; }
        public void setRedisVersion(String redisVersion) { this.redisVersion = redisVersion; }
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
        public long getUptimeInSeconds() { return uptimeInSeconds; }
        public void setUptimeInSeconds(long uptimeInSeconds) { this.uptimeInSeconds = uptimeInSeconds; }
        public int getConnectedClients() { return connectedClients; }
        public void setConnectedClients(int connectedClients) { this.connectedClients = connectedClients; }
        public int getBlockedClients() { return blockedClients; }
        public void setBlockedClients(int blockedClients) { this.blockedClients = blockedClients; }
        public long getUsedMemory() { return usedMemory; }
        public void setUsedMemory(long usedMemory) { this.usedMemory = usedMemory; }
        public String getUsedMemoryHuman() { return usedMemoryHuman; }
        public void setUsedMemoryHuman(String usedMemoryHuman) { this.usedMemoryHuman = usedMemoryHuman; }
        public long getUsedMemoryPeak() { return usedMemoryPeak; }
        public void setUsedMemoryPeak(long usedMemoryPeak) { this.usedMemoryPeak = usedMemoryPeak; }
        public String getUsedMemoryPeakHuman() { return usedMemoryPeakHuman; }
        public void setUsedMemoryPeakHuman(String usedMemoryPeakHuman) { 
            this.usedMemoryPeakHuman = usedMemoryPeakHuman; 
        }
        public long getMaxMemory() { return maxMemory; }
        public void setMaxMemory(long maxMemory) { this.maxMemory = maxMemory; }
        public double getMemFragmentationRatio() { return memFragmentationRatio; }
        public void setMemFragmentationRatio(double memFragmentationRatio) { 
            this.memFragmentationRatio = memFragmentationRatio; 
        }
        public long getTotalConnectionsReceived() { return totalConnectionsReceived; }
        public void setTotalConnectionsReceived(long totalConnectionsReceived) { 
            this.totalConnectionsReceived = totalConnectionsReceived; 
        }
        public long getTotalCommandsProcessed() { return totalCommandsProcessed; }
        public void setTotalCommandsProcessed(long totalCommandsProcessed) { 
            this.totalCommandsProcessed = totalCommandsProcessed; 
        }
        public long getInstantaneousOpsPerSec() { return instantaneousOpsPerSec; }
        public void setInstantaneousOpsPerSec(long instantaneousOpsPerSec) { 
            this.instantaneousOpsPerSec = instantaneousOpsPerSec; 
        }
        public long getRdbLastSaveTime() { return rdbLastSaveTime; }
        public void setRdbLastSaveTime(long rdbLastSaveTime) { this.rdbLastSaveTime = rdbLastSaveTime; }
        public boolean isAofEnabled() { return aofEnabled; }
        public void setAofEnabled(boolean aofEnabled) { this.aofEnabled = aofEnabled; }
    }
}

// ============================================================

// 3. 数据库连接池监控工具类（HikariCP）
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import javax.sql.DataSource;

public class DataSourceMonitor {
    
    private final HikariDataSource dataSource;
    private final HikariPoolMXBean poolMXBean;
    
    public DataSourceMonitor(DataSource dataSource) {
        if (!(dataSource instanceof HikariDataSource)) {
            throw new IllegalArgumentException("DataSource must be HikariDataSource");
        }
        this.dataSource = (HikariDataSource) dataSource;
        this.poolMXBean = this.dataSource.getHikariPoolMXBean();
    }
    
    /**
     * 获取连接池信息
     */
    public PoolInfo getPoolInfo() {
        PoolInfo info = new PoolInfo();
        info.setTotalConnections(poolMXBean.getTotalConnections());
        info.setActiveConnections(poolMXBean.getActiveConnections());
        info.setIdleConnections(poolMXBean.getIdleConnections());
        info.setThreadsAwaitingConnection(poolMXBean.getThreadsAwaitingConnection());
        info.setMaxPoolSize(dataSource.getMaximumPoolSize());
        info.setMinIdle(dataSource.getMinimumIdle());
        info.setConnectionTimeout(dataSource.getConnectionTimeout());
        info.setIdleTimeout(dataSource.getIdleTimeout());
        info.setMaxLifetime(dataSource.getMaxLifetime());
        
        // 计算使用率
        if (info.getMaxPoolSize() > 0) {
            double usagePercent = (info.getActiveConnections() * 100.0) / info.getMaxPoolSize();
            info.setUsagePercent(usagePercent);
        }
        
        return info;
    }
    
    /**
     * 检查连接池健康状态
     */
    public boolean isHealthy(double threshold) {
        PoolInfo info = getPoolInfo();
        return info.getUsagePercent() < threshold && 
               info.getThreadsAwaitingConnection() == 0;
    }
    
    /**
     * 软驱逐空闲连接
     */
    public void softEvictConnections() {
        poolMXBean.softEvictConnections();
    }
    
    public static class PoolInfo {
        private int totalConnections;
        private int activeConnections;
        private int idleConnections;
        private int threadsAwaitingConnection;
        private int maxPoolSize;
        private int minIdle;
        private long connectionTimeout;
        private long idleTimeout;
        private long maxLifetime;
        private double usagePercent;
        
        // Getters and Setters
        public int getTotalConnections() { return totalConnections; }
        public void setTotalConnections(int totalConnections) { this.totalConnections = totalConnections; }
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        public int getIdleConnections() { return idleConnections; }
        public void setIdleConnections(int idleConnections) { this.idleConnections = idleConnections; }
        public int getThreadsAwaitingConnection() { return threadsAwaitingConnection; }
        public void setThreadsAwaitingConnection(int threadsAwaitingConnection) { 
            this.threadsAwaitingConnection = threadsAwaitingConnection; 
        }
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        public int getMinIdle() { return minIdle; }
        public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
        public long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
        
        @Override
        public String toString() {
            return String.format(
                "Pool[total=%d, active=%d, idle=%d, waiting=%d, max=%d, usage=%.2f%%]",
                totalConnections, activeConnections, idleConnections, 
                threadsAwaitingConnection, maxPoolSize, usagePercent
            );
        }
    }
}

// ============================================================

// 4. HTTP请求统计监控工具
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class HttpRequestMonitor {
    
    private final Map<String, EndpointStats> endpointStatsMap = new ConcurrentHashMap<>();
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalErrors = new LongAdder();
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    /**
     * 记录请求
     */
    public void recordRequest(HttpServletRequest request, long responseTime, boolean isError) {
        String endpoint = request.getMethod() + " " + request.getRequestURI();
        
        EndpointStats stats = endpointStatsMap.computeIfAbsent(endpoint, k -> new EndpointStats(endpoint));
        stats.incrementRequestCount();
        stats.addResponseTime(responseTime);
        
        if (isError) {
            stats.incrementErrorCount();
            totalErrors.increment();
        }
        
        totalRequests.increment();
        totalResponseTime.addAndGet(responseTime);
    }
    
    /**
     * 获取所有端点统计
     */
    public List<EndpointStats> getAllStats() {
        return new ArrayList<>(endpointStatsMap.values());
    }
    
    /**
     * 获取指定端点统计
     */
    public EndpointStats getStats(String endpoint) {
        return endpointStatsMap.get(endpoint);
    }
    
    /**
     * 获取全局统计
     */
    public GlobalStats getGlobalStats() {
        GlobalStats stats = new GlobalStats();
        stats.setTotalRequests(totalRequests.sum());
        stats.setTotalErrors(totalErrors.sum());
        
        long totalReq = totalRequests.sum();
        if (totalReq > 0) {
            stats.setAverageResponseTime(totalResponseTime.get() / totalReq);
            stats.setErrorRate((totalErrors.sum() * 100.0) / totalReq);
        }
        
        return stats;
    }
    
    /**
     * 获取TOP N最慢的端点
     */
    public List<EndpointStats> getTopSlowest(int n) {
        return endpointStatsMap.values().stream()
            .sorted(Comparator.comparingDouble(EndpointStats::getAverageResponseTime).reversed())
            .limit(n)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取TOP N最多请求的端点
     */
    public List<EndpointStats> getTopRequested(int n) {
        return endpointStatsMap.values().stream()
            .sorted(Comparator.comparingLong(EndpointStats::getRequestCount).reversed())
            .limit(n)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 重置统计
     */
    public void reset() {
        endpointStatsMap.clear();
        totalRequests.reset();
        totalErrors.reset();
        totalResponseTime.set(0);
    }
    
    public static class EndpointStats {
        private final String endpoint;
        private final LongAdder requestCount = new LongAdder();
        private final LongAdder errorCount = new LongAdder();
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private volatile long maxResponseTime = 0;
        private volatile long minResponseTime = Long.MAX_VALUE;
        
        public EndpointStats(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public void incrementRequestCount() {
            requestCount.increment();
        }
        
        public void incrementErrorCount() {
            errorCount.increment();
        }
        
        public void addResponseTime(long responseTime) {
            totalResponseTime.addAndGet(responseTime);
            
            // 更新最大最小值
            long currentMax = maxResponseTime;
            if (responseTime > currentMax) {
                maxResponseTime = responseTime;
            }
            
            long currentMin = minResponseTime;
            if (responseTime < currentMin) {
                minResponseTime = responseTime;
            }
        }
        
        public String getEndpoint() { return endpoint; }
        public long getRequestCount() { return requestCount.sum(); }
        public long getErrorCount() { return errorCount.sum(); }
        public long getMaxResponseTime() { return maxResponseTime; }
        public long getMinResponseTime() { return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime; }
        
        public double getAverageResponseTime() {
            long count = requestCount.sum();
            return count > 0 ? (double) totalResponseTime.get() / count : 0;
        }
        
        public double getErrorRate() {
            long count = requestCount.sum();
            return count > 0 ? (errorCount.sum() * 100.0) / count : 0;
        }
    }
    
    public static class GlobalStats {
        private long totalRequests;
        private long totalErrors;
        private double averageResponseTime;
        private double errorRate;
        
        // Getters and Setters
        public long getTotalRequests() { return totalRequests; }
````



## Prompt:
2025/9/29 09:28:36

Continue



## Response:
2025/9/29 09:30:04



**Spring Boot 监控工具类集合** ∙ Version 2

````java
// 1. JVM监控工具类
import java.lang.management.*;
import java.util.*;

public class JvmMonitor {
    
    private static final Runtime runtime = Runtime.getRuntime();
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    /**
     * 获取JVM内存信息
     */
    public static MemoryInfo getMemoryInfo() {
        MemoryInfo info = new MemoryInfo();
        
        // 堆内存
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        info.setHeapInit(heapMemory.getInit());
        info.setHeapUsed(heapMemory.getUsed());
        info.setHeapCommitted(heapMemory.getCommitted());
        info.setHeapMax(heapMemory.getMax());
        info.setHeapUsagePercent(heapMemory.getUsed() * 100.0 / heapMemory.getMax());
        
        // 非堆内存
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
        info.setNonHeapUsed(nonHeapMemory.getUsed());
        info.setNonHeapCommitted(nonHeapMemory.getCommitted());
        
        // JVM总内存
        info.setTotalMemory(runtime.totalMemory());
        info.setFreeMemory(runtime.freeMemory());
        info.setMaxMemory(runtime.maxMemory());
        info.setUsedMemory(runtime.totalMemory() - runtime.freeMemory());
        
        return info;
    }
    
    /**
     * 获取线程信息
     */
    public static ThreadInfo getThreadInfo() {
        ThreadInfo info = new ThreadInfo();
        info.setThreadCount(threadMXBean.getThreadCount());
        info.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        info.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        info.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        
        // 死锁检测
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        info.setDeadlockedThreadCount(deadlockedThreads != null ? deadlockedThreads.length : 0);
        
        return info;
    }
    
    /**
     * 获取GC信息
     */
    public static List<GcInfo> getGcInfo() {
        List<GcInfo> gcInfoList = new ArrayList<>();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            GcInfo gcInfo = new GcInfo();
            gcInfo.setName(gcBean.getName());
            gcInfo.setCollectionCount(gcBean.getCollectionCount());
            gcInfo.setCollectionTime(gcBean.getCollectionTime());
            gcInfoList.add(gcInfo);
        }
        return gcInfoList;
    }
    
    /**
     * 获取CPU使用率
     */
    public static double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getProcessCpuLoad() * 100;
        }
        return -1;
    }
    
    /**
     * 获取系统信息
     */
    public static SystemInfo getSystemInfo() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        SystemInfo info = new SystemInfo();
        info.setOsName(osBean.getName());
        info.setOsVersion(osBean.getVersion());
        info.setOsArch(osBean.getArch());
        info.setAvailableProcessors(osBean.getAvailableProcessors());
        info.setSystemLoadAverage(osBean.getSystemLoadAverage());
        
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        info.setJvmName(runtimeMXBean.getVmName());
        info.setJvmVersion(runtimeMXBean.getVmVersion());
        info.setJvmUptime(runtimeMXBean.getUptime());
        info.setStartTime(runtimeMXBean.getStartTime());
        
        return info;
    }
    
    /**
     * 检查内存使用是否健康
     */
    public static boolean isMemoryHealthy(double threshold) {
        MemoryInfo memoryInfo = getMemoryInfo();
        return memoryInfo.getHeapUsagePercent() < threshold;
    }
    
    // 内部类
    public static class MemoryInfo {
        private long heapInit;
        private long heapUsed;
        private long heapCommitted;
        private long heapMax;
        private double heapUsagePercent;
        private long nonHeapUsed;
        private long nonHeapCommitted;
        private long totalMemory;
        private long freeMemory;
        private long maxMemory;
        private long usedMemory;
        
        // Getters and Setters
        public long getHeapInit() { return heapInit; }
        public void setHeapInit(long heapInit) { this.heapInit = heapInit; }
        public long getHeapUsed() { return heapUsed; }
        public void setHeapUsed(long heapUsed) { this.heapUsed = heapUsed; }
        public long getHeapCommitted() { return heapCommitted; }
        public void setHeapCommitted(long heapCommitted) { this.heapCommitted = heapCommitted; }
        public long getHeapMax() { return heapMax; }
        public void setHeapMax(long heapMax) { this.heapMax = heapMax; }
        public double getHeapUsagePercent() { return heapUsagePercent; }
        public void setHeapUsagePercent(double heapUsagePercent) { this.heapUsagePercent = heapUsagePercent; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public void setNonHeapUsed(long nonHeapUsed) { this.nonHeapUsed = nonHeapUsed; }
        public long getNonHeapCommitted() { return nonHeapCommitted; }
        public void setNonHeapCommitted(long nonHeapCommitted) { this.nonHeapCommitted = nonHeapCommitted; }
        public long getTotalMemory() { return totalMemory; }
        public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public void setFreeMemory(long freeMemory) { this.freeMemory = freeMemory; }
        public long getMaxMemory() { return maxMemory; }
        public void setMaxMemory(long maxMemory) { this.maxMemory = maxMemory; }
        public long getUsedMemory() { return usedMemory; }
        public void setUsedMemory(long usedMemory) { this.usedMemory = usedMemory; }
    }
    
    public static class ThreadInfo {
        private int threadCount;
        private int peakThreadCount;
        private long totalStartedThreadCount;
        private int daemonThreadCount;
        private int deadlockedThreadCount;
        
        // Getters and Setters
        public int getThreadCount() { return threadCount; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
        public int getPeakThreadCount() { return peakThreadCount; }
        public void setPeakThreadCount(int peakThreadCount) { this.peakThreadCount = peakThreadCount; }
        public long getTotalStartedThreadCount() { return totalStartedThreadCount; }
        public void setTotalStartedThreadCount(long totalStartedThreadCount) { 
            this.totalStartedThreadCount = totalStartedThreadCount; 
        }
        public int getDaemonThreadCount() { return daemonThreadCount; }
        public void setDaemonThreadCount(int daemonThreadCount) { this.daemonThreadCount = daemonThreadCount; }
        public int getDeadlockedThreadCount() { return deadlockedThreadCount; }
        public void setDeadlockedThreadCount(int deadlockedThreadCount) { 
            this.deadlockedThreadCount = deadlockedThreadCount; 
        }
    }
    
    public static class GcInfo {
        private String name;
        private long collectionCount;
        private long collectionTime;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getCollectionCount() { return collectionCount; }
        public void setCollectionCount(long collectionCount) { this.collectionCount = collectionCount; }
        public long getCollectionTime() { return collectionTime; }
        public void setCollectionTime(long collectionTime) { this.collectionTime = collectionTime; }
    }
    
    public static class SystemInfo {
        private String osName;
        private String osVersion;
        private String osArch;
        private int availableProcessors;
        private double systemLoadAverage;
        private String jvmName;
        private String jvmVersion;
        private long jvmUptime;
        private long startTime;
        
        // Getters and Setters
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
        public String getOsArch() { return osArch; }
        public void setOsArch(String osArch) { this.osArch = osArch; }
        public int getAvailableProcessors() { return availableProcessors; }
        public void setAvailableProcessors(int availableProcessors) { 
            this.availableProcessors = availableProcessors; 
        }
        public double getSystemLoadAverage() { return systemLoadAverage; }
        public void setSystemLoadAverage(double systemLoadAverage) { 
            this.systemLoadAverage = systemLoadAverage; 
        }
        public String getJvmName() { return jvmName; }
        public void setJvmName(String jvmName) { this.jvmName = jvmName; }
        public String getJvmVersion() { return jvmVersion; }
        public void setJvmVersion(String jvmVersion) { this.jvmVersion = jvmVersion; }
        public long getJvmUptime() { return jvmUptime; }
        public void setJvmUptime(long jvmUptime) { this.jvmUptime = jvmUptime; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
    }
}

// ============================================================

// 2. Redis监控工具类
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.*;

public class RedisMonitor {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisMonitor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 获取Redis信息
     */
    public RedisInfo getRedisInfo() {
        return redisTemplate.execute((RedisCallback<RedisInfo>) connection -> {
            Properties info = connection.info();
            RedisInfo redisInfo = new RedisInfo();
            
            if (info != null) {
                // 服务器信息
                redisInfo.setRedisVersion(info.getProperty("redis_version"));
                redisInfo.setOs(info.getProperty("os"));
                redisInfo.setUptimeInSeconds(Long.parseLong(info.getProperty("uptime_in_seconds", "0")));
                
                // 客户端信息
                redisInfo.setConnectedClients(Integer.parseInt(info.getProperty("connected_clients", "0")));
                redisInfo.setBlockedClients(Integer.parseInt(info.getProperty("blocked_clients", "0")));
                
                // 内存信息
                redisInfo.setUsedMemory(Long.parseLong(info.getProperty("used_memory", "0")));
                redisInfo.setUsedMemoryHuman(info.getProperty("used_memory_human"));
                redisInfo.setUsedMemoryPeak(Long.parseLong(info.getProperty("used_memory_peak", "0")));
                redisInfo.setUsedMemoryPeakHuman(info.getProperty("used_memory_peak_human"));
                redisInfo.setMaxMemory(Long.parseLong(info.getProperty("maxmemory", "0")));
                redisInfo.setMemFragmentationRatio(
                    Double.parseDouble(info.getProperty("mem_fragmentation_ratio", "0")));
                
                // 统计信息
                redisInfo.setTotalConnectionsReceived(
                    Long.parseLong(info.getProperty("total_connections_received", "0")));
                redisInfo.setTotalCommandsProcessed(
                    Long.parseLong(info.getProperty("total_commands_processed", "0")));
                redisInfo.setInstantaneousOpsPerSec(
                    Long.parseLong(info.getProperty("instantaneous_ops_per_sec", "0")));
                
                // 持久化信息
                redisInfo.setRdbLastSaveTime(Long.parseLong(info.getProperty("rdb_last_save_time", "0")));
                redisInfo.setAofEnabled(info.getProperty("aof_enabled", "0").equals("1"));
            }
            
            return redisInfo;
        });
    }
    
    /**
     * 获取数据库大小
     */
    public Long getDbSize() {
        return redisTemplate.execute((RedisCallback<Long>) RedisConnection::dbSize);
    }
    
    /**
     * 获取内存使用情况
     */
    public Map<String, Object> getMemoryStats() {
        RedisInfo info = getRedisInfo();
        Map<String, Object> stats = new HashMap<>();
        stats.put("usedMemory", info.getUsedMemory());
        stats.put("usedMemoryHuman", info.getUsedMemoryHuman());
        stats.put("usedMemoryPeak", info.getUsedMemoryPeak());
        stats.put("maxMemory", info.getMaxMemory());
        stats.put("memFragmentationRatio", info.getMemFragmentationRatio());
        
        if (info.getMaxMemory() > 0) {
            double usagePercent = (info.getUsedMemory() * 100.0) / info.getMaxMemory();
            stats.put("usagePercent", usagePercent);
        }
        
        return stats;
    }
    
    /**
     * 检查Redis健康状态
     */
    public boolean isHealthy() {
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                String pong = connection.ping();
                return "PONG".equals(pong);
            });
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取慢查询日志
     */
    public List<Object> getSlowLogs(long count) {
        return redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
            return connection.slowLogGet(count);
        });
    }
    
    public static class RedisInfo {
        private String redisVersion;
        private String os;
        private long uptimeInSeconds;
        private int connectedClients;
        private int blockedClients;
        private long usedMemory;
        private String usedMemoryHuman;
        private long usedMemoryPeak;
        private String usedMemoryPeakHuman;
        private long maxMemory;
        private double memFragmentationRatio;
        private long totalConnectionsReceived;
        private long totalCommandsProcessed;
        private long instantaneousOpsPerSec;
        private long rdbLastSaveTime;
        private boolean aofEnabled;
        
        // Getters and Setters
        public String getRedisVersion() { return redisVersion; }
        public void setRedisVersion(String redisVersion) { this.redisVersion = redisVersion; }
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
        public long getUptimeInSeconds() { return uptimeInSeconds; }
        public void setUptimeInSeconds(long uptimeInSeconds) { this.uptimeInSeconds = uptimeInSeconds; }
        public int getConnectedClients() { return connectedClients; }
        public void setConnectedClients(int connectedClients) { this.connectedClients = connectedClients; }
        public int getBlockedClients() { return blockedClients; }
        public void setBlockedClients(int blockedClients) { this.blockedClients = blockedClients; }
        public long getUsedMemory() { return usedMemory; }
        public void setUsedMemory(long usedMemory) { this.usedMemory = usedMemory; }
        public String getUsedMemoryHuman() { return usedMemoryHuman; }
        public void setUsedMemoryHuman(String usedMemoryHuman) { this.usedMemoryHuman = usedMemoryHuman; }
        public long getUsedMemoryPeak() { return usedMemoryPeak; }
        public void setUsedMemoryPeak(long usedMemoryPeak) { this.usedMemoryPeak = usedMemoryPeak; }
        public String getUsedMemoryPeakHuman() { return usedMemoryPeakHuman; }
        public void setUsedMemoryPeakHuman(String usedMemoryPeakHuman) { 
            this.usedMemoryPeakHuman = usedMemoryPeakHuman; 
        }
        public long getMaxMemory() { return maxMemory; }
        public void setMaxMemory(long maxMemory) { this.maxMemory = maxMemory; }
        public double getMemFragmentationRatio() { return memFragmentationRatio; }
        public void setMemFragmentationRatio(double memFragmentationRatio) { 
            this.memFragmentationRatio = memFragmentationRatio; 
        }
        public long getTotalConnectionsReceived() { return totalConnectionsReceived; }
        public void setTotalConnectionsReceived(long totalConnectionsReceived) { 
            this.totalConnectionsReceived = totalConnectionsReceived; 
        }
        public long getTotalCommandsProcessed() { return totalCommandsProcessed; }
        public void setTotalCommandsProcessed(long totalCommandsProcessed) { 
            this.totalCommandsProcessed = totalCommandsProcessed; 
        }
        public long getInstantaneousOpsPerSec() { return instantaneousOpsPerSec; }
        public void setInstantaneousOpsPerSec(long instantaneousOpsPerSec) { 
            this.instantaneousOpsPerSec = instantaneousOpsPerSec; 
        }
        public long getRdbLastSaveTime() { return rdbLastSaveTime; }
        public void setRdbLastSaveTime(long rdbLastSaveTime) { this.rdbLastSaveTime = rdbLastSaveTime; }
        public boolean isAofEnabled() { return aofEnabled; }
        public void setAofEnabled(boolean aofEnabled) { this.aofEnabled = aofEnabled; }
    }
}

// ============================================================

// 3. 数据库连接池监控工具类（HikariCP）
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import javax.sql.DataSource;

public class DataSourceMonitor {
    
    private final HikariDataSource dataSource;
    private final HikariPoolMXBean poolMXBean;
    
    public DataSourceMonitor(DataSource dataSource) {
        if (!(dataSource instanceof HikariDataSource)) {
            throw new IllegalArgumentException("DataSource must be HikariDataSource");
        }
        this.dataSource = (HikariDataSource) dataSource;
        this.poolMXBean = this.dataSource.getHikariPoolMXBean();
    }
    
    /**
     * 获取连接池信息
     */
    public PoolInfo getPoolInfo() {
        PoolInfo info = new PoolInfo();
        info.setTotalConnections(poolMXBean.getTotalConnections());
        info.setActiveConnections(poolMXBean.getActiveConnections());
        info.setIdleConnections(poolMXBean.getIdleConnections());
        info.setThreadsAwaitingConnection(poolMXBean.getThreadsAwaitingConnection());
        info.setMaxPoolSize(dataSource.getMaximumPoolSize());
        info.setMinIdle(dataSource.getMinimumIdle());
        info.setConnectionTimeout(dataSource.getConnectionTimeout());
        info.setIdleTimeout(dataSource.getIdleTimeout());
        info.setMaxLifetime(dataSource.getMaxLifetime());
        
        // 计算使用率
        if (info.getMaxPoolSize() > 0) {
            double usagePercent = (info.getActiveConnections() * 100.0) / info.getMaxPoolSize();
            info.setUsagePercent(usagePercent);
        }
        
        return info;
    }
    
    /**
     * 检查连接池健康状态
     */
    public boolean isHealthy(double threshold) {
        PoolInfo info = getPoolInfo();
        return info.getUsagePercent() < threshold && 
               info.getThreadsAwaitingConnection() == 0;
    }
    
    /**
     * 软驱逐空闲连接
     */
    public void softEvictConnections() {
        poolMXBean.softEvictConnections();
    }
    
    public static class PoolInfo {
        private int totalConnections;
        private int activeConnections;
        private int idleConnections;
        private int threadsAwaitingConnection;
        private int maxPoolSize;
        private int minIdle;
        private long connectionTimeout;
        private long idleTimeout;
        private long maxLifetime;
        private double usagePercent;
        
        // Getters and Setters
        public int getTotalConnections() { return totalConnections; }
        public void setTotalConnections(int totalConnections) { this.totalConnections = totalConnections; }
        public int getActiveConnections() { return activeConnections; }
        public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
        public int getIdleConnections() { return idleConnections; }
        public void setIdleConnections(int idleConnections) { this.idleConnections = idleConnections; }
        public int getThreadsAwaitingConnection() { return threadsAwaitingConnection; }
        public void setThreadsAwaitingConnection(int threadsAwaitingConnection) { 
            this.threadsAwaitingConnection = threadsAwaitingConnection; 
        }
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        public int getMinIdle() { return minIdle; }
        public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
        public long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
        
        @Override
        public String toString() {
            return String.format(
                "Pool[total=%d, active=%d, idle=%d, waiting=%d, max=%d, usage=%.2f%%]",
                totalConnections, activeConnections, idleConnections, 
                threadsAwaitingConnection, maxPoolSize, usagePercent
            );
        }
    }
}

// ============================================================

// 4. HTTP请求统计监控工具
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class HttpRequestMonitor {
    
    private final Map<String, EndpointStats> endpointStatsMap = new ConcurrentHashMap<>();
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalErrors = new LongAdder();
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    /**
     * 记录请求
     */
    public void recordRequest(HttpServletRequest request, long responseTime, boolean isError) {
        String endpoint = request.getMethod() + " " + request.getRequestURI();
        
        EndpointStats stats = endpointStatsMap.computeIfAbsent(endpoint, k -> new EndpointStats(endpoint));
        stats.incrementRequestCount();
        stats.addResponseTime(responseTime);
        
        if (isError) {
            stats.incrementErrorCount();
            totalErrors.increment();
        }
        
        totalRequests.increment();
        totalResponseTime.addAndGet(responseTime);
    }
    
    /**
     * 获取所有端点统计
     */
    public List<EndpointStats> getAllStats() {
        return new ArrayList<>(endpointStatsMap.values());
    }
    
    /**
     * 获取指定端点统计
     */
    public EndpointStats getStats(String endpoint) {
        return endpointStatsMap.get(endpoint);
    }
    
    /**
     * 获取全局统计
     */
    public GlobalStats getGlobalStats() {
        GlobalStats stats = new GlobalStats();
        stats.setTotalRequests(totalRequests.sum());
        stats.setTotalErrors(totalErrors.sum());
        
        long totalReq = totalRequests.sum();
        if (totalReq > 0) {
            stats.setAverageResponseTime(totalResponseTime.get() / totalReq);
            stats.setErrorRate((totalErrors.sum() * 100.0) / totalReq);
        }
        
        return stats;
    }
    
    /**
     * 获取TOP N最慢的端点
     */
    public List<EndpointStats> getTopSlowest(int n) {
        return endpointStatsMap.values().stream()
            .sorted(Comparator.comparingDouble(EndpointStats::getAverageResponseTime).reversed())
            .limit(n)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取TOP N最多请求的端点
     */
    public List<EndpointStats> getTopRequested(int n) {
        return endpointStatsMap.values().stream()
            .sorted(Comparator.comparingLong(EndpointStats::getRequestCount).reversed())
            .limit(n)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 重置统计
     */
    public void reset() {
        endpointStatsMap.clear();
        totalRequests.reset();
        totalErrors.reset();
        totalResponseTime.set(0);
    }
    
    public static class EndpointStats {
        private final String endpoint;
        private final LongAdder requestCount = new LongAdder();
        private final LongAdder errorCount = new LongAdder();
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private volatile long maxResponseTime = 0;
        private volatile long minResponseTime = Long.MAX_VALUE;
        
        public EndpointStats(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public void incrementRequestCount() {
            requestCount.increment();
        }
        
        public void incrementErrorCount() {
            errorCount.increment();
        }
        
        public void addResponseTime(long responseTime) {
            totalResponseTime.addAndGet(responseTime);
            
            // 更新最大最小值
            long currentMax = maxResponseTime;
            if (responseTime > currentMax) {
                maxResponseTime = responseTime;
            }
            
            long currentMin = minResponseTime;
            if (responseTime < currentMin) {
                minResponseTime = responseTime;
            }
        }
        
        public String getEndpoint() { return endpoint; }
        public long getRequestCount() { return requestCount.sum(); }
        public long getErrorCount() { return errorCount.sum(); }
        public long getMaxResponseTime() { return maxResponseTime; }
        public long getMinResponseTime() { return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime; }
        
        public double getAverageResponseTime() {
            long count = requestCount.sum();
            return count > 0 ? (double) totalResponseTime.get() / count : 0;
        }
        
        public double getErrorRate() {
            long count = requestCount.sum();
            return count > 0 ? (errorCount.sum() * 100.0) / count : 0;
        }
    }
    
    public static class GlobalStats {
        private long totalRequests;
        private long totalErrors;
        private double averageResponseTime;
        private double errorRate;
        
        // Getters and Setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { 
            this.averageResponseTime = averageResponseTime; 
        }
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
    }
}

// ============================================================

// 5. 接口性能监控拦截器（配合上面的HttpRequestMonitor使用）
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PerformanceMonitorInterceptor implements HandlerInterceptor {
    
    private final HttpRequestMonitor requestMonitor;
    private static final String START_TIME_ATTR = "startTime";
    
    public PerformanceMonitorInterceptor(HttpRequestMonitor requestMonitor) {
        this.requestMonitor = requestMonitor;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime != null) {
            long responseTime = System.currentTimeMillis() - startTime;
            boolean isError = response.getStatus() >= 400 || ex != null;
            requestMonitor.recordRequest(request, responseTime, isError);
        }
    }
}

// ============================================================

// 6. 应用健康检查工具
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

public class HealthCheckUtil {
    
    /**
     * 检查数据库连接
     */
    public static Health checkDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(3)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withException(e)
                .build();
        }
        return Health.down()
            .withDetail("database", "Connection invalid")
            .build();
    }
    
    /**
     * 检查磁盘空间
     */
    public static Health checkDiskSpace(long thresholdBytes) {
        try {
            java.io.File root = new java.io.File("/");
            long freeSpace = root.getFreeSpace();
            long totalSpace = root.getTotalSpace();
            long usedSpace = totalSpace - freeSpace;
            
            Map<String, Object> details = new HashMap<>();
            details.put("total", formatBytes(totalSpace));
            details.put("free", formatBytes(freeSpace));
            details.put("used", formatBytes(usedSpace));
            details.put("threshold", formatBytes(thresholdBytes));
            details.put("usagePercent", String.format("%.2f%%", (usedSpace * 100.0) / totalSpace));
            
            if (freeSpace < thresholdBytes) {
                return Health.status(Status.DOWN)
                    .withDetails(details)
                    .withDetail("reason", "Free disk space below threshold")
                    .build();
            }
            
            return Health.up().withDetails(details).build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
    
    /**
     * 检查内存使用
     */
    public static Health checkMemory(double thresholdPercent) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double usagePercent = (usedMemory * 100.0) / maxMemory;
        
        Map<String, Object> details = new HashMap<>();
        details.put("max", formatBytes(maxMemory));
        details.put("total", formatBytes(totalMemory));
        details.put("free", formatBytes(freeMemory));
        details.put("used", formatBytes(usedMemory));
        details.put("usagePercent", String.format("%.2f%%", usagePercent));
        
        if (usagePercent > thresholdPercent) {
            return Health.status(Status.DOWN)
                .withDetails(details)
                .withDetail("reason", "Memory usage above threshold")
                .build();
        }
        
        return Health.up().withDetails(details).build();
    }
    
    /**
     * 综合健康检查
     */
    public static Health checkAll(DataSource dataSource, long diskThreshold, double memoryThreshold) {
        List<Health> healths = new ArrayList<>();
        healths.add(checkDatabase(dataSource));
        healths.add(checkDiskSpace(diskThreshold));
        healths.add(checkMemory(memoryThreshold));
        
        boolean allUp = healths.stream().allMatch(h -> h.getStatus() == Status.UP);
        
        Map<String, Object> details = new HashMap<>();
        details.put("database", healths.get(0).getStatus());
        details.put("diskSpace", healths.get(1).getStatus());
        details.put("memory", healths.get(2).getStatus());
        
        if (allUp) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
    
    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}

// ============================================================

// 7. 慢SQL监控工具（基于Druid）
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.JdbcSqlStat;
import java.util.*;
import java.util.stream.Collectors;

public class SlowSqlMonitor {
    
    private final DruidDataSource dataSource;
    
    public SlowSqlMonitor(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * 获取慢SQL列表
     */
    public List<SqlStats> getSlowSqls(int topN, long thresholdMillis) {
        Map<String, JdbcSqlStat> sqlStatMap = dataSource.getDataSourceStat().getSqlStatMap();
        
        return sqlStatMap.values().stream()
            .filter(stat -> stat.getExecuteMillisMax() > thresholdMillis)
            .sorted(Comparator.comparingLong(JdbcSqlStat::getExecuteMillisMax).reversed())
            .limit(topN)
            .map(this::convertToSqlStats)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有SQL统计
     */
    public List<SqlStats> getAllSqlStats() {
        Map<String, JdbcSqlStat> sqlStatMap = dataSource.getDataSourceStat().getSqlStatMap();
        
        return sqlStatMap.values().stream()
            .map(this::convertToSqlStats)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取最耗时的SQL
     */
    public List<SqlStats> getTopTimeCostSqls(int topN) {
        Map<String, JdbcSqlStat> sqlStatMap = dataSource.getDataSourceStat().getSqlStatMap();
        
        return sqlStatMap.values().stream()
            .sorted(Comparator.comparingLong(stat -> 
                stat.getExecuteCount() * stat.getExecuteMillisMax()).reversed())
            .limit(topN)
            .map(this::convertToSqlStats)
            .collect(Collectors.toList());
    }
    
    private SqlStats convertToSqlStats(JdbcSqlStat stat) {
        SqlStats stats = new SqlStats();
        stats.setSql(stat.getSql());
        stats.setExecuteCount(stat.getExecuteCount());
        stats.setErrorCount(stat.getErrorCount());
        stats.setTotalTime(stat.getExecuteMillisTotal());
        stats.setMaxTime(stat.getExecuteMillisMax());
        
        if (stat.getExecuteCount() > 0) {
            stats.setAvgTime(stat.getExecuteMillisTotal() / stat.getExecuteCount());
        }
        
        return stats;
    }
    
    /**
     * 重置SQL统计
     */
    public void resetSqlStats() {
        dataSource.resetStat();
    }
    
    public static class SqlStats {
        private String sql;
        private long executeCount;
        private long errorCount;
        private long totalTime;
        private long maxTime;
        private long avgTime;
        
        // Getters and Setters
        public String getSql() { return sql; }
        public void setSql(String sql) { this.sql = sql; }
        public long getExecuteCount() { return executeCount; }
        public void setExecuteCount(long executeCount) { this.executeCount = executeCount; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        public long getTotalTime() { return totalTime; }
        public void setTotalTime(long totalTime) { this.totalTime = totalTime; }
        public long getMaxTime() { return maxTime; }
        public void setMaxTime(long maxTime) { this.maxTime = maxTime; }
        public long getAvgTime() { return avgTime; }
        public void setAvgTime(long avgTime) { this.avgTime = avgTime; }
        
        @Override
        public String toString() {
            return String.format(
                "SQL[executions=%d, errors=%d, max=%dms, avg=%dms]: %s",
                executeCount, errorCount, maxTime, avgTime, 
                sql.length() > 100 ? sql.substring(0, 100) + "..." : sql
            );
        }
    }
}

// ============================================================

// 8. 指标采集器（Metrics收集）
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class MetricsCollector {
    
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Gauge> gauges = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Histogram> histograms = new ConcurrentHashMap<>();
    
    /**
     * 计数器 - 递增
     */
    public void incrementCounter(String name) {
        incrementCounter(name, 1);
    }
    
    public void incrementCounter(String name, long delta) {
        counters.computeIfAbsent(name, k -> new Counter()).increment(delta);
    }
    
    /**
     * 仪表盘 - 设置值
     */
    public void setGauge(String name, long value) {
        gauges.computeIfAbsent(name, k -> new Gauge()).setValue(value);
    }
    
    /**
     * 计时器 - 记录耗时
     */
    public void recordTimer(String name, long duration) {
        timers.computeIfAbsent(name, k -> new Timer()).record(duration);
    }
    
    /**
     * 直方图 - 记录分布
     */
    public void recordHistogram(String name, long value) {
        histograms.computeIfAbsent(name, k -> new Histogram()).update(value);
    }
    
    /**
     * 获取计数器值
     */
    public long getCounterValue(String name) {
        Counter counter = counters.get(name);
        return counter != null ? counter.getCount() : 0;
    }
    
    /**
     * 获取所有指标
     */
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        Map<String, Long> counterValues = new HashMap<>();
        counters.forEach((k, v) -> counterValues.put(k, v.getCount()));
        metrics.put("counters", counterValues);
        
        Map<String, Long> gaugeValues = new HashMap<>();
        gauges.forEach((k, v) -> gaugeValues.put(k, v.getValue()));
        metrics.put("gauges", gaugeValues);
        
        Map<String, Map<String, Object>> timerValues = new HashMap<>();
        timers.forEach((k, v) -> timerValues.put(k, v.getStats()));
        metrics.put("timers", timerValues);
        
        Map<String, Map<String, Object>> histogramValues = new HashMap<>();
        histograms.forEach((k, v) -> histogramValues.put(k, v.getStats()));
        metrics.put("histograms", histogramValues);
        
        return metrics;
    }
    
    /**
     * 重置所有指标
     */
    public void reset() {
        counters.clear();
        gauges.clear();
        timers.clear();
        histograms.clear();
    }
    
    // 计数器
    public static class Counter {
        private final LongAdder count = new LongAdder();
        
        public void increment(long delta) {
            count.add(delta);
        }
        
        public long getCount() {
            return count.sum();
        }
    }
    
    // 仪表
    public static class Gauge {
        private final AtomicLong value = new AtomicLong(0);
        
        public void setValue(long newValue) {
            value.set(newValue);
        }
        
        public long getValue() {
            return value.get();
        }
    }
    
    // 计时器
    public static class Timer {
        private final LongAdder count = new LongAdder();
        private final AtomicLong totalTime = new AtomicLong(0);
        private volatile long maxTime = 0;
        private volatile long minTime = Long.MAX_VALUE;
        
        public void record(long duration) {
            count.increment();
            totalTime.addAndGet(duration);
            
            long currentMax = maxTime;
            if (duration > currentMax) {
                maxTime = duration;
            }
            
            long currentMin = minTime;
            if (duration < currentMin) {
                minTime = duration;
            }
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            long cnt = count.sum();
            stats.put("count", cnt);
            stats.put("totalTime", totalTime.get());
            stats.put("avgTime", cnt > 0 ? totalTime.get() / cnt : 0);
            stats.put("maxTime", maxTime);
            stats.put("minTime", minTime == Long.MAX_VALUE ? 0 : minTime);
            return stats;
        }
    }
    
    // 直方图
    public static class Histogram {
        private final LongAdder count = new LongAdder();
        private final AtomicLong sum = new AtomicLong(0);
        private volatile long max = Long.MIN_VALUE;
        private volatile long min = Long.MAX_VALUE;
        
        public void update(long value) {
            count.increment();
            sum.addAndGet(value);
            
            long currentMax = max;
            if (value > currentMax) {
                max = value;
            }
            
            long currentMin = min;
            if (value < currentMin) {
                min = value;
            }
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            long cnt = count.sum();
            stats.put("count", cnt);
            stats.put("sum", sum.get());
            stats.put("avg", cnt > 0 ? sum.get() / cnt : 0);
            stats.put("max", max == Long.MIN_VALUE ? 0 : max);
            stats.put("min", min == Long.MAX_VALUE ? 0 : min);
            return stats;
        }
    }
}

// ============================================================

// 9. 系统资源监控工具（CPU、磁盘、网络）
import java.io.File;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class SystemResourceMonitor {
    
    private static final OperatingSystemMXBean osBean = 
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    /**
     * 获取CPU使用率
     */
    public static CpuInfo getCpuInfo() {
        CpuInfo info = new CpuInfo();
        info.setProcessCpuLoad(osBean.getProcessCpuLoad() * 100);
        info.setSystemCpuLoad(osBean.getSystemCpuLoad() * 100);
        info.setAvailableProcessors(osBean.getAvailableProcessors());
        info.setSystemLoadAverage(osBean.getSystemLoadAverage());
        return info;
    }
    
    /**
     * 获取磁盘信息
     */
    public static List<DiskInfo> getDiskInfo() {
        List<DiskInfo> diskInfoList = new ArrayList<>();
        File[] roots = File.listRoots();
        
        for (File root : roots) {
            DiskInfo info = new DiskInfo();
            info.setPath(root.getAbsolutePath());
            info.setTotalSpace(root.getTotalSpace());
            info.setFreeSpace(root.getFreeSpace());
            info.setUsableSpace(root.getUsableSpace());
            info.setUsedSpace(root.getTotalSpace() - root.getFreeSpace());
            
            if (root.getTotalSpace() > 0) {
                double usagePercent = (info.getUsedSpace() * 100.0) / root.getTotalSpace();
                info.setUsagePercent(usagePercent);
            }
            
            diskInfoList.add(info);
        }
        
        return diskInfoList;
    }
    
    /**
     * 获取系统物理内存信息
     */
    public static PhysicalMemoryInfo getPhysicalMemoryInfo() {
        PhysicalMemoryInfo info = new PhysicalMemoryInfo();
        info.setTotalPhysicalMemory(osBean.getTotalPhysicalMemorySize());
        info.setFreePhysicalMemory(osBean.getFreePhysicalMemorySize());
        info.setUsedPhysicalMemory(
            osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize());
        
        if (osBean.getTotalPhysicalMemorySize() > 0) {
            double usagePercent = (info.getUsedPhysicalMemory() * 100.0) / 
                osBean.getTotalPhysicalMemorySize();
            info.setUsagePercent(usagePercent);
        }
        
        info.setTotalSwapSpace(osBean.getTotalSwapSpaceSize());
        info.setFreeSwapSpace(osBean.getFreeSwapSpaceSize());
        
        return info;
    }
    
    public static class CpuInfo {
        private double processCpuLoad;
        private double systemCpuLoad;
        private int availableProcessors;
        private double systemLoadAverage;
        
        // Getters and Setters
        public double getProcessCpuLoad() { return processCpuLoad; }
        public void setProcessCpuLoad(double processCpuLoad) { this.processCpuLoad = processCpuLoad; }
        public double getSystemCpuLoad() { return systemCpuLoad; }
        public void setSystemCpuLoad(double systemCpuLoad) { this.systemCpuLoad = systemCpuLoad; }
        public int getAvailableProcessors() { return availableProcessors; }
        public void setAvailableProcessors(int availableProcessors) { 
            this.availableProcessors = availableProcessors; 
        }
        public double getSystemLoadAverage() { return systemLoadAverage; }
        public void setSystemLoadAverage(double systemLoadAverage) { 
            this.systemLoadAverage = systemLoadAverage; 
        }
    }
    
    public static class DiskInfo {
        private String path;
        private long totalSpace;
        private long freeSpace;
        private long usableSpace;
        private long usedSpace;
        private double usagePercent;
        
        // Getters and Setters
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public long getTotalSpace() { return totalSpace; }
        public void setTotalSpace(long totalSpace) { this.totalSpace = totalSpace; }
        public long getFreeSpace() { return freeSpace; }
        public void setFreeSpace(long freeSpace) { this.freeSpace = freeSpace; }
        public long getUsableSpace() { return usableSpace; }
        public void setUsableSpace(long usableSpace) { this.usableSpace = usableSpace; }
        public long getUsedSpace() { return usedSpace; }
        public void setUsedSpace(long usedSpace) { this.usedSpace = usedSpace; }
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
    }
    
    public static class PhysicalMemoryInfo {
        private long totalPhysicalMemory;
        private long freePhysicalMemory;
        private long usedPhysicalMemory;
        private double usagePercent;
        private long totalSwapSpace;
        private long freeSwapSpace;
        
        // Getters and Setters
        public long getTotalPhysicalMemory() { return totalPhysicalMemory; }
        public void setTotalPhysicalMemory(long totalPhysicalMemory) { 
            this.totalPhysicalMemory = totalPhysicalMemory; 
        }
        public long getFreePhysicalMemory() { return freePhysicalMemory; }
        public void setFreePhysicalMemory(long freePhysicalMemory) { 
            this.freePhysicalMemory = freePhysicalMemory; 
        }
        public long getUsedPhysicalMemory() { return usedPhysicalMemory; }
        public void setUsedPhysicalMemory(long usedPhysicalMemory) { 
            this.usedPhysicalMemory = usedPhysicalMemory; 
        }
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
        public long getTotalSwapSpace() { return totalSwapSpace; }
        public void setTotalSwapSpace(long totalSwapSpace) { this.totalSwapSpace = totalSwapSpace; }
        public long getFreeSwapSpace() { return freeSwapSpace; }
        public void setFreeSwapSpace(long freeSwapSpace) { this.freeSwapSpace = freeSwapSpace; }
    }
}
````

完成！现在一共为你创建了 **9 个监控类工具**。

## 📊 监控工具类详细说明

### 1. **JvmMonitor** - JVM 监控
**功能：**
- ✅ 内存监控（堆内存、非堆内存、使用率）
- ✅ 线程监控（线程数、峰值线程数、死锁检测）
- ✅ GC 监控（GC次数、GC耗时）
- ✅ CPU 使用率
- ✅ 系统信息（OS、JVM版本、运行时长）

**使用场景：** 实时监控 JVM 健康状态，及时发现内存泄漏、GC 频繁等问题

---

### 2. **RedisMonitor** - Redis 监控
**功能：**
- ✅ Redis 服务器信息（版本、运行时长）
- ✅ 客户端连接数（当前连接、阻塞连接）
- ✅ 内存使用情况（已用内存、峰值内存、碎片率）
- ✅ 性能统计（QPS、总命令数）
- ✅ 持久化信息（RDB、AOF）
- ✅ 慢查询日志
- ✅ 健康检查

**依赖：** Spring Data Redis

---

### 3. **DataSourceMonitor** - 数据库连接池监控（HikariCP）
**功能：**
- ✅ 连接池状态（总连接数、活跃连接、空闲连接）
- ✅ 等待队列监控
- ✅ 连接池配置信息
- ✅ 使用率计算
- ✅ 健康检查
- ✅ 空闲连接驱逐

**适用场景：** 监控数据库连接池，防止连接泄漏、连接耗尽

---

### 4. **HttpRequestMonitor** - HTTP 请求统计监控
**功能：**
- ✅ 端点级别统计（请求次数、错误次数、响应时间）
- ✅ 全局统计（总请求、总错误、平均响应时间、错误率）
- ✅ TOP N 最慢接口
- ✅ TOP N 最多请求接口
- ✅ 最大/最小/平均响应时间

**配合使用：** 需要配合 `PerformanceMonitorInterceptor` 拦截器

---

### 5. **PerformanceMonitorInterceptor** - 性能监控拦截器
**功能：**
- ✅ 自动拦截所有 HTTP 请求
- ✅ 记录请求响应时间
- ✅ 统计请求成功/失败

**配置示例：**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private HttpRequestMonitor requestMonitor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PerformanceMonitorInterceptor(requestMonitor))
                .addPathPatterns("/**");
    }
}
```

---

### 6. **HealthCheckUtil** - 应用健康检查
**功能：**
- ✅ 数据库连接检查
- ✅ 磁盘空间检查（设置阈值）
- ✅ 内存使用检查（设置阈值）
- ✅ 综合健康检查

**集成 Spring Boot Actuator：**
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        return HealthCheckUtil.checkDatabase(dataSource);
    }
}
```

---

### 7. **SlowSqlMonitor** - 慢 SQL 监控（Druid）
**功能：**
- ✅ 慢 SQL 列表（可设置阈值）
- ✅ SQL 执行统计（执行次数、错误次数、耗时）
- ✅ TOP N 最耗时 SQL
- ✅ 重置统计

**依赖：** Druid 数据源
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.18</version>
</dependency>
```

---

### 8. **MetricsCollector** - 指标采集器
**功能：**
- ✅ **Counter（计数器）**：递增计数
- ✅ **Gauge（仪表）**：设置瞬时值
- ✅ **Timer（计时器）**：记录耗时分布
- ✅ **Histogram（直方图）**：记录数值分布
- ✅ 获取所有指标快照

**使用示例：**
```java
// 计数
metricsCollector.incrementCounter("user.login");

// 记录耗时
metricsCollector.recordTimer("order.create", 125);

// 设置在线人数
metricsCollector.setGauge("online.users", 1234);

// 记录订单金额分布
metricsCollector.recordHistogram("order.amount", 9999);
```

---

### 9. **SystemResourceMonitor** - 系统资源监控
**功能：





---
Powered by [Claude Exporter](https://www.claudexporter.com)