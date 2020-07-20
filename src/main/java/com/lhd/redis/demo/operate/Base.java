package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.LogFactory;
import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.Map;

/**
 * redis 基础数据结构
 *
 * @author lhd
 */
public class Base {

    public static void main(String[] args) throws InterruptedException {
        stringOperate();
        listOperate();
        hashOperate();
        setOperate();
        zsetOperate();
    }

    /**
     * zset 相关操作
     */
    private static void zsetOperate() {
        Jedis jedis = RedisFactory.getInstance();

        //清空所有数据 切记在测试数据库运行
        jedis.flushDB();

        //设置
        jedis.zadd("a", 10.0, "a");
        jedis.zadd("a", 9.0, "b");
        jedis.zadd("a", 8.0, "c");

        //获取所有
        LogFactory.info("a所有数据 = {}", jedis.zrange("a", 0, -1));

        //逆序获取所有
        LogFactory.info("a所有数据 = {}", jedis.zrevrange("a", 0, -1));

        //长度
        LogFactory.info("a所有长度 = {}", jedis.zcard("a"));

        //获取排序值
        LogFactory.info("a中c的排序值 = {}", jedis.zscore("a", "c"));

        //获取排名
        LogFactory.info("a中c的排名 = {}", jedis.zrank("a", "c"));

        //删除数据
        jedis.zrem("a", "c");
        LogFactory.info("a所有数据 = {}", jedis.zrevrange("a", 0, -1));

        //根据分数范围获取
        jedis.zadd("a", 7, "d");
        jedis.zadd("a", 6, "e");
        jedis.zadd("a", 5, "f");
        LogFactory.info("a范围数据 = {}", jedis.zrangeByScoreWithScores("a", 7, 9));
    }

    /**
     * set 相关操作
     */
    private static void setOperate() {
        Jedis jedis = RedisFactory.getInstance();

        //清空所有数据 切记在测试数据库运行
        jedis.flushDB();

        //设置值
        jedis.sadd("a", "a", "a", "b");

        //获取所有
        LogFactory.info("a的所有数据 = {}", jedis.smembers("a"));

        //是否存在
        LogFactory.info("a的是否存在b = {}", jedis.sismember("a", "b"));
        LogFactory.info("a的是否存在c = {}", jedis.sismember("a", "c"));

        //a的长度
        LogFactory.info("a的长度 = {}", jedis.scard("a"));

        //弹出数据 随机
        LogFactory.info(jedis.spop("a"));
        LogFactory.info("a的所有数据 = {}", jedis.smembers("a"));
    }

    /**
     * hash 相关操作
     */
    private static void hashOperate() {
        Jedis jedis = RedisFactory.getInstance();

        //清空所有数据 切记在测试数据库运行
        jedis.flushDB();

        //设置值
        jedis.hset("a", "a", "a");
        LogFactory.info(jedis.hget("a", "a"));

        //获取所有
        jedis.hset("a", "b", "b");
        LogFactory.info("获取所有{}", jedis.hgetAll("a").toString());

        //获取所有
        jedis.hset("a", "c", "c");
        LogFactory.info("a长度 = {}", jedis.hlen("a"));

        //批量设置
        Map<String, String> params = new HashMap<String, String>(4);
        params.put("d", "d");
        params.put("e", "e");
        jedis.hmset("a", params);
        LogFactory.info("d和e = {}", jedis.hmget("a", "d", "e"));

    }

    /**
     * list 相关操作
     */
    private static void listOperate() {
        Jedis jedis = RedisFactory.getInstance();

        //清空所有数据 切记在测试数据库运行
        jedis.flushDB();

        //右边进左边出：队列
        jedis.rpush("a", "a", "b", "c");
        LogFactory.info("出队 1 = {}, 2 = {}. 3 = {}", jedis.lpop("a"), jedis.lpop("a"), jedis.lpop("a"));

        //右边进右边出：栈
        jedis.rpush("b", "a", "b", "c");
        LogFactory.info("出栈 1 = {}, 2 = {}. 3 = {}", jedis.rpop("b"), jedis.rpop("b"), jedis.rpop("b"));

        //索引定位
        jedis.rpush("c", "a", "b", "c");
        LogFactory.info("第二个是 {}", jedis.lindex("c", 0));

        //范围获取
        jedis.rpush("d", "a", "b", "c");
        LogFactory.info("d中所有数据是 {}", jedis.lrange("d", 0, 1));

        //获取长度
        jedis.rpush("e", "a", "b", "c");
        LogFactory.info("e有{}数量", jedis.llen("e"));

        //保留指定长度
        jedis.rpush("f", "a", "b", "c");
        jedis.ltrim("f", 1, 2);
        LogFactory.info("f保留的数据为{}", jedis.lrange("f", 0, -1));
    }

    /**
     * string 相关操作
     */
    private static void stringOperate() throws InterruptedException {
        Jedis jedis = RedisFactory.getInstance();

        //清空所有数据 切记在测试数据库运行
        jedis.flushDB();

        //键值对
        jedis.set("a", "a");
        LogFactory.info(jedis.get("a"));

        //批量键值对
        jedis.mset("b", "b", "c", "c", "d", "d");
        LogFactory.info(jedis.mget("b", "c", "d").toString());

        //设置过期时间 ex 设置秒级 px 设置毫秒级
        jedis.set("e", "e", SetParams.setParams().ex(5));
        LogFactory.info(jedis.get("e"));
        Thread.sleep(5000L);
        LogFactory.info("e 是否存在 = {}", jedis.get("e") != null);

        //不存在则设置
        jedis.set("f", "f", SetParams.setParams().nx());
        LogFactory.info(jedis.get("f"));
        jedis.set("f", "ffffff", SetParams.setParams().nx());
        LogFactory.info("nx下f是否设置成功 {}， f = {}", "ffffff".equals(jedis.get("f")), jedis.get("f"));

        //整数自增
        jedis.set("g", "1");
        jedis.incr("g");
        LogFactory.info("g = {}", jedis.get("g"));
        jedis.incrBy("g", 5);
        LogFactory.info("g = {}", jedis.get("g"));
    }

}
