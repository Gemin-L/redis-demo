package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.LogFactory;
import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.Jedis;

/**
 * 不精确的统计
 *
 * @author lhd
 */
public class HyperLogLog {

    public static void main(String[] args) {

        Jedis jedis = RedisFactory.getInstance();

        //请在测试环境运行
        jedis.flushDB();

        for (int i = 1; i <= 1000; i++) {
            jedis.pfadd("test", "test" + i);
            long pfcount = jedis.pfcount("test");
            LogFactory.info("应有数量{}, 实际数量{},准确率{}", i, pfcount, pfcount*1.0/i*100);
        }

    }

}
