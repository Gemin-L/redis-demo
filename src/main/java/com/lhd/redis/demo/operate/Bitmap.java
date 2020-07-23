package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.LogFactory;
import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.Random;

/**
 * 位图 字节数组的结构 底层使用的是string
 * 一般用于统计、签到等场景
 *
 * @author lhd
 */
public class Bitmap {


    public static void main(String[] args) {
        Jedis jedis = RedisFactory.getInstance();
        //情况数据库 请在测试环境操作
        jedis.flushDB();

        /*
            假设有一千个用户 id 1-1000 有100个标签 1-100
            统计拥有 1、50、99标签的用户
         */
        for (int i = 0; i < 100; i++) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.multi();
            for (int j = 0; j < 1000; j++) {
                Random random = new Random();
                int nextInt = random.nextInt(random.nextInt(100) + 1);
                //随机模拟是否有该标签
                pipeline.setbit("tag" + i, j, nextInt > random.nextInt(random.nextInt(50) + 1));
            }
            pipeline.exec();
            pipeline.close();
            LogFactory.info("tag{} 该标签人数:{}", i, jedis.bitcount("tag" + i));
        }

        jedis.bitop(BitOP.AND, "test", "tag1", "tag50", "tag99");

        Long result = jedis.bitcount("test");
        LogFactory.info("1、50、99标签的用户人数 result = {}", result);
        for (int i = 0; i < 1000; i++) {
            LogFactory.info("用户id = {}, 是否满足：{}", i,  jedis.getbit("test", i));
        }
    }


}
