package com.lhd.redis.demo.factory;

/**
 * 日志打印
 *
 * @author lhd
 */
public class LogFactory {

    /**
     * 日志打印
     *
     * @param main   主句
     * @param params 参数
     */
    public static void info(String main, Object... params) {
        String result = main.replaceAll("\\{}", "%s");
        System.out.println(String.format(result, params));
    }

}
