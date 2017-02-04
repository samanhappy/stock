package com.samanhappy.stock.storage;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient
{

    private static Logger logger = LoggerFactory.getLogger(RedisClient.class);

    private static JedisPool pool;

    static
    {
        ResourceBundle bundle = ResourceBundle.getBundle("redis");
        if (bundle == null)
        {
            throw new IllegalArgumentException("[redis.properties] is not found!");
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Integer.valueOf(bundle.getString("redis.pool.maxActive")));
        poolConfig.setMaxIdle(Integer.valueOf(bundle.getString("redis.pool.maxIdle")));
        poolConfig.setMinIdle(Integer.valueOf(bundle.getString("redis.pool.minIdle")));
        poolConfig.setMaxWaitMillis(Long.valueOf(bundle.getString("redis.pool.maxWait")));
        poolConfig.setTestOnBorrow(Boolean.valueOf(bundle.getString("redis.pool.testOnBorrow")));
        poolConfig.setTestOnReturn(Boolean.valueOf(bundle.getString("redis.pool.testOnReturn")));
        if ("SAM-PC".equals(System.getenv("COMPUTERNAME")))
        {
            pool = new JedisPool(poolConfig, bundle.getString("redis.ip.sam-pc"), Integer.valueOf(bundle
                    .getString("redis.port")), 10 * 1000, bundle.getString("redis.password"));
        }
        else
        {
            pool = new JedisPool(poolConfig, bundle.getString("redis.ip.default"), Integer.valueOf(bundle
                    .getString("redis.port")), 10 * 1000);
        }
    }

    public static void sadd(String key, String... members)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.sadd(key, members);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }

    public static void expire(String key, int seconds)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.expire(key, seconds);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }
    
    public static void del(String key)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.del(key);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }

    public static void lpush(String key, String... members)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.lpush(key, members);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }

    public static void srem(String key, String... members)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.srem(key, members);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }

    public static void hset(String key, String field, String value)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.hset(key, field, value);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }

    public static String hget(String key, String field)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.hget(key, field);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
        return null;
    }

    public static boolean hexists(String key, String field)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.hexists(key, field);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
        return false;
    }

    public static boolean sismember(String key, String field)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.sismember(key, field);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
        return false;
    }

    public static Set<String> smembers(String key)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.smembers(key);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
        return null;
    }
    
    public static Set<String> keys(String pattern)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.keys(pattern);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
        return null;
    }

    public static String get(String key)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.get(key);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
        return null;
    }

    public static void set(String key, String value)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.set(key, value);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }

    public static long incr(String key)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.incr(key);
        }
        catch (Exception e)
        {
            logger.error("", e);
            return 0;
        }
        finally
        {
            jedis.close();
        }
    }

    public static long hincr(String key, String field, int i)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.hincrBy(key, field, i);
        }
        catch (Exception e)
        {
            logger.error("", e);
            return 0;
        }
        finally
        {
            jedis.close();
        }
    }

    public static Set<String> hkeys(String key)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.hkeys(key);
        }
        catch (Exception e)
        {
            logger.error("", e);
            return null;
        }
        finally
        {
            jedis.close();
        }
    }

    public static boolean exists(String key)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.exists(key);
        }
        catch (Exception e)
        {
            logger.error("", e);
            return false;
        }
        finally
        {
            jedis.close();
        }
    }

    public static List<String> lrange(String key, long start, long end)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            return jedis.lrange(key, start, end);
        }
        catch (Exception e)
        {
            logger.error("", e);
            return null;
        }
        finally
        {
            jedis.close();
        }
    }

    public static void hdel(String key, String... fields)
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            jedis.hdel(key, fields);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        finally
        {
            jedis.close();
        }
    }

    public static void main(String[] args)
    {
        RedisClient.get("text");
    }
}
