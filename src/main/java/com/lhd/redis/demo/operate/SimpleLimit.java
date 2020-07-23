package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.LogFactory;
import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.Jedis;

/**
 * 简单的接口限流
 * 每个用户针对每个接口 多少秒内限制访问次数
 *
 * @author lhd
 */
public class SimpleLimit {

    private Jedis jedis;

    public SimpleLimit(Jedis jedis) {
        this.jedis = jedis;
        jedis.flushDB();
    }

    public Boolean isActionAllowed(String userId, String actionKey, Integer time, Integer maxCount) {

        String key = userId + "_" + actionKey;

        long currentTimeMillis = System.currentTimeMillis();

        //删除这个时间之前的访问次数
        jedis.zremrangeByScore(key, 0, currentTimeMillis - time * 1000);

        jedis.zadd(key, currentTimeMillis, currentTimeMillis + "");
        //如果没访问 time秒后失效
        jedis.expire(key, time);
        return jedis.zcard(key) <= maxCount;

    }


    public static void main(String[] args) throws InterruptedException {
        SimpleLimit simpleLimit = new SimpleLimit(RedisFactory.getInstance());
        String userId = "123";
        String actionKey = "test";
        for (int i = 0; i < 100; i++) {
            Boolean allowed = simpleLimit.isActionAllowed(userId, actionKey, 10, 10);
            LogFactory.info("是否允许访问{}", allowed);
        }
        //十秒后看看是否可以访问
        Thread.sleep(10000);
        for (int i = 0; i < 100; i++) {
            Boolean allowed = simpleLimit.isActionAllowed(userId, actionKey, 10, 10);
            LogFactory.info("是否允许访问{}", allowed);
        }
    }
}
