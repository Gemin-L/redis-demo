package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.Jedis;

/**
 * 不精确的判断是否存在
 * 布隆过滤器
 *
 * @author lhd
 */
public class BloomFilter {

    public static void main(String[] args) {
        Jedis jedis = RedisFactory.getInstance();
        //请在测试环境运行
        jedis.flushDB();

    }

}
