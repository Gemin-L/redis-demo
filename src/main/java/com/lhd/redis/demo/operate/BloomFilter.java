package com.lhd.redis.demo.operate;


import com.lhd.redis.demo.factory.LogFactory;
import io.rebloom.client.Client;

/**
 * 不精确的判断是否存在
 * 布隆过滤器
 * <p>
 * 对于已有的数据 不会进行误判 对于不存在的数据 会出现误判
 * 也就是 如果不存在 则肯定不存在 如果存在 则可能存在 因为存在可能误判
 *
 * @author lhd
 */
public class BloomFilter {

    public static void main(String[] args) {
        Client client = new Client("localhost", 6379);

        client.delete("test");

        for (int i = 0; i < 1000; i++) {
            client.add("test", "user" + i);
            LogFactory.info("判断是否存在{}, {}", "user" + i, client.exists("test", "user" + i));
        }

        int count = 0;
        for (int i = 1000; i < 2000; i++) {
            boolean test = client.exists("test", "user" + i);
            LogFactory.info("判断是否存在{}, {}", "user" + i, test);
            if (test) {
                count++;
                LogFactory.info("这是一个误判, {}", "user" + i);
            }
        }
        LogFactory.info("误判的比率{}", count * 1.00 / 1000 * 100);

        client.delete("test1");
        //进行误判比率的调整 容量越大 错误率越低
        client.createFilter("test1", 2000, 0.01);
        for (int i = 0; i < 1000; i++) {
            client.add("test1", "user" + i);
            LogFactory.info("判断是否存在{}, {}", "user" + i, client.exists("test", "user" + i));
        }

        count = 0;
        for (int i = 1000; i < 2000; i++) {
            boolean test = client.exists("test1", "user" + i);
            LogFactory.info("判断是否存在{}, {}", "user" + i, test);
            if (test) {
                count++;
                LogFactory.info("这是一个误判, {}", "user" + i);
            }
        }
        LogFactory.info("误判的比率{}", count * 1.00 / 1000 * 100);
    }

}
