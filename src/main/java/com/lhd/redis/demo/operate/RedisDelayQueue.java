package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.LogFactory;
import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis 实现的延迟队列
 * 通过zset结构 value设置为执行时间 通过定时接口轮询执行的任务
 * 问题：多实例部署的时候需要考虑并发性的问题
 *
 * @author lhd
 */
public class RedisDelayQueue {

    private Jedis jedis;

    public RedisDelayQueue(Jedis jedis) {
        this.jedis = jedis;

        //为了测试方便 情况数据库 切记使用测试环境
        this.jedis.flushDB();
    }

    /**
     * 放入一个任务
     *
     * @param key      延迟队列的key
     * @param taskName 任务名称 这边暂时不放具体的任务
     * @param execTime 执行时间
     * @return 是否成功
     */
    public Boolean input(String key, String taskName, Long execTime) {
        jedis.zadd(key, execTime, taskName);
        return Boolean.TRUE;
    }

    /**
     * 取出任务
     *
     * @param key      延迟队列的key
     * @param execTime 执行时间 小于该时间的任务都取出来
     * @return 任务
     */
    public List<String> outPut(String key, Long execTime) {
        //把这个事件点之前的任务 找出来执行  也可以自行调整
        Set<Tuple> result = jedis.zrangeByScoreWithScores(key, 0, execTime);
        List<String> list = result.stream().map(Tuple::getElement).collect(Collectors.toList());
        if (!list.isEmpty()){
            for (String s : list) {
                jedis.zrem(key, s);
            }
        }
        return list;
    }


    public static void main(String[] args) throws InterruptedException {
        RedisDelayQueue redisDelayQueue = new RedisDelayQueue(RedisFactory.getInstance());

        long currentTimeMillis = System.currentTimeMillis();
        //放置十个任务
        for (int i = 0; i < 10; i++) {
            Random random = new Random();
            long l = currentTimeMillis + i * 1000 *  random.nextInt(5);
            redisDelayQueue.input("test", "我是任务" + i + ",我的执行之间是 " + l, l);
        }

        //这里应该是另外的线程一直跑 这里偷偷懒
        for (int i = 0; i < 200; i++) {
            Thread.sleep(1000);
            List<String> list = redisDelayQueue.outPut("test", System.currentTimeMillis());
            for (String result : list) {
                LogFactory.info("当前时间是{}， 任务被执行了 task = {}", System.currentTimeMillis(), result);
            }
        }

    }

}
