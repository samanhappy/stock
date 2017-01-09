package com.samanhappy.stock.storage;

import redis.clients.jedis.Jedis;

public class RedisSupport
{

    private static Jedis jedis;

    static
    {
        jedis = new Jedis("192.168.10.170", 6379);
        jedis.auth("123456");
    }

    public static Jedis getJedis()
    {
        return jedis;
    }
}
