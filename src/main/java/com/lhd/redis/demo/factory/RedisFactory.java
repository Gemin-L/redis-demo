package com.lhd.redis.demo.factory;

import redis.clients.jedis.Jedis;

/**
 * redis 客户端生成
 *
 * @author lhd
 */
public class RedisFactory {

    /**
     * 生成
     *
     * @return 客户端
     */
    public static Jedis getInstance() {
        return new Jedis("127.0.0.1");
    }

}
