package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.LogFactory;
import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

/**
 * redis 锁简单实现
 * 1.通过设置nx  key不存在则设置成功 存在则设置失败
 * 2.设置ex 过期时间 防止获取锁的线程异常 造成死锁
 * 3.没获取到锁的线程 只能轮询+休眠去获取锁
 * 4.业务结束 根据value去删除锁
 * <p>
 * 问题：设置了锁的过期时间 所以如果业务逻辑过长导致的超过时间 可能存在新线程获取到锁的问题
 *
 * @author lhd
 */
public class Lock {

    private Jedis jedis;

    public Lock(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * 加锁
     *
     * @param key  锁的key
     * @param uuid 锁的值
     * @return 是否成功
     */
    public Boolean lock(String key, String uuid) {
        String result = jedis.set(key, uuid, SetParams.setParams().nx().ex(5));
        LogFactory.info("获取锁key = {}, value  = {}, result = {}", key, uuid, result);
        return "OK".equals(result);
    }

    /**
     * 解锁
     *
     * @param key  锁的key
     * @param uuid 锁的值
     * @return 是否成功
     */
    public Boolean unLock(String key, String uuid) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(uuid));
        LogFactory.info("释放锁key = {}, value  = {}, result = {}", key, uuid, result);
        return "1".equals(result.toString());
    }


    public static void main(String[] args) {
//        test1();
        test2();
    }

    /**
     * 模拟业务 多线程获取锁
     */
    private static void test2() {
        //清空所有数据 切记在测试数据库运行
        Jedis jedis = RedisFactory.getInstance();
        jedis.flushDB();

        Thread thread1 = new Thread(() -> {
            try {
                Jedis jedis1 = RedisFactory.getInstance();
                Lock lock1 = new Lock(jedis1);
                String uuid1 = UUID.randomUUID().toString();
                Boolean lock1Bool = lock1.lock("test", uuid1);
                LogFactory.info("thread1获取锁是否成功 success = {}", lock1Bool);
                while (!lock1Bool){
                    //睡眠一秒 继续获取
                    Thread.sleep(1000);
                    lock1Bool = lock1.lock("test", uuid1);
                }
                Thread.sleep(3000);
                //三秒后释放锁
                Boolean unLock1Bool = lock1.unLock("test", uuid1);
                LogFactory.info("thread1释放锁是否成功 success = {}", unLock1Bool);
            }catch (Exception e){
                e.printStackTrace();
            }

        });

        Thread thread2 = new Thread(() -> {
            try {
                Jedis jedis2 = RedisFactory.getInstance();
                Lock lock2 = new Lock(jedis2);
                String uuid2 = UUID.randomUUID().toString();
                Boolean lock2Bool = lock2.lock("test", uuid2);
                LogFactory.info("thread2获取锁是否成功 success = {}", lock2Bool);
                while (!lock2Bool){
                    //睡眠一秒 继续获取
                    Thread.sleep(1000);
                    lock2Bool = lock2.lock("test", uuid2);
                }
                Thread.sleep(3000);
                //三秒后释放锁
                Boolean unLock2Bool = lock2.unLock("test", uuid2);
                LogFactory.info("thread2释放锁是否成功 success = {}", unLock2Bool);
            }catch (Exception e){
                e.printStackTrace();
            }

        });

        thread1.start();
        thread2.start();
    }

    /**
     * 简单的测试
     */
    private static void test1() {
        Jedis jedis = RedisFactory.getInstance();

        //清空所有数据 切记在测试数据库运行
        jedis.flushDB();

        //加锁
        Lock lock1 = new Lock(jedis);
        String uuid1 = UUID.randomUUID().toString();
        Boolean lock1Bool = lock1.lock("test", uuid1);
        LogFactory.info("获取锁是否成功 success = {}", lock1Bool);

        Lock lock2 = new Lock(jedis);
        String uuid2 = UUID.randomUUID().toString();
        Boolean lock2Bool = lock2.lock("test", uuid2);
        LogFactory.info("获取锁是否成功 success = {}", lock2Bool);

        //释放锁
        Boolean unLock1Bool = lock1.unLock("test", uuid1);
        LogFactory.info("释放锁是否成功 success = {}", unLock1Bool);

        Boolean unLock2Bool = lock2.unLock("test", uuid1);
        LogFactory.info("释放锁是否成功 success = {}", unLock2Bool);
    }
}
