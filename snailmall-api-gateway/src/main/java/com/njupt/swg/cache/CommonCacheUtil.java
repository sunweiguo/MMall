package com.njupt.swg.cache;

import com.njupt.swg.exception.SnailmallException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Author swg.
 * @Date 2019/1/1 15:03
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Component
@Slf4j
public class CommonCacheUtil {

    @Autowired
    private JedisPoolWrapper jedisPoolWrapper;


    /**
     * 缓存永久key
     */
    public void cache(String key, String value) {
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis Jedis = pool.getResource()) {
                    Jedis.select(0);
                    Jedis.set(key, value);
                }
            }
        } catch (Exception e) {
            log.error("redis存值失败", e);
            throw new SnailmallException("redis报错");
        }
    }

    /**
     * 获取缓存key
     */
    public String getCacheValue(String key) {
        String value = null;
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis Jedis = pool.getResource()) {
                    Jedis.select(0);
                    value = Jedis.get(key);
                }
            }
        } catch (Exception e) {
            log.error("redis获取指失败", e);
            throw new SnailmallException("redis报错");
        }
        return value;
    }

    /**
     * 过期key
     */
    public long cacheNxExpire(String key, String value, int expire) {
        long result = 0;
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.select(0);
                    result = jedis.setnx(key, value);
                    jedis.expire(key, expire);
                }
            }
        } catch (Exception e) {
            log.error("redis塞值和设置缓存时间失败", e);
            throw new SnailmallException("redis报错");
        }

        return result;
    }

    /**
     * 删除缓存key
     */
    public void delKey(String key) {
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);
                try {
                    jedis.del(key);
                } catch (Exception e) {
                    log.error("从redis中删除失败", e);
                    throw new SnailmallException("redis报错");
                }
            }
        }
    }



}
