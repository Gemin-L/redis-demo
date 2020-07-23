package com.lhd.redis.demo.operate;

import com.lhd.redis.demo.factory.LogFactory;
import com.lhd.redis.demo.factory.RedisFactory;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.GeoRadiusParam;

import java.util.List;
import java.util.Random;

/**
 * 地理位置相关
 *
 * @author lhd
 */
public class Geohash {


    public static void main(String[] args) {

        Jedis jedis = RedisFactory.getInstance();
        //请在测试环境运行
        jedis.flushDB();

        Pipeline pipelined = jedis.pipelined();
        pipelined.multi();
        for (int i = 1; i <= 100; i++) {
            Random random = new Random();
            int nextInt = random.nextInt(9);
            //随机设置坐标
            pipelined.geoadd("test", Double.parseDouble("118." + i * nextInt), Double.parseDouble("37." + i * nextInt), i + "");
        }
        pipelined.exec();
        pipelined.close();

        //计算距离
        Double geodist = jedis.geodist("test", "1", "100", GeoUnit.M);
        LogFactory.info("1和100相差{}M", geodist);

        //获取元素
        LogFactory.info("50的坐标为{} }", jedis.geopos("test", "50").get(0).getLongitude(), jedis.geopos("test", "50").get(0).getLatitude());

        //根据元素查询附件元素georadiusbymember 会包括自己
        List<GeoRadiusResponse> list = jedis.georadiusByMember("test", "50", 20, GeoUnit.KM, GeoRadiusParam.geoRadiusParam().withCoord().withDist().sortAscending());
        LogFactory.info("50元素附件20KM内的元素个数 {}", list.size());
        for (GeoRadiusResponse geoRadiusResponse : list) {
            LogFactory.info("50元素附件20KM内的元素为{},距离{},坐标{} {}", geoRadiusResponse.getMemberByString(), geoRadiusResponse.getDistance(), geoRadiusResponse.getCoordinate().getLongitude(), geoRadiusResponse.getCoordinate().getLatitude());
        }

        //坐标查询附件元素
        List<GeoRadiusResponse> result = jedis.georadius("test", 118.20000261068344, 37.19999990441611, 20, GeoUnit.KM, GeoRadiusParam.geoRadiusParam().withCoord().withDist().sortAscending());
        LogFactory.info("附件20KM内的元素个数 {}", result.size());
        for (GeoRadiusResponse geoRadiusResponse : result) {
            LogFactory.info("附件20KM内的元素为{},距离{},坐标{} {}", geoRadiusResponse.getMemberByString(), geoRadiusResponse.getDistance(), geoRadiusResponse.getCoordinate().getLongitude(), geoRadiusResponse.getCoordinate().getLatitude());
        }
    }

}
