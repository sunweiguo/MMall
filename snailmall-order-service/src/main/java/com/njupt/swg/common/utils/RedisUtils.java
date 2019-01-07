package com.njupt.swg.common.utils;

import com.njupt.swg.cache.JedisPoolWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collections;

/**
 * @Author swg.
 * @Date 2019/1/7 12:23
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Component
public class RedisUtils {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private JedisPoolWrapper jedisPoolWrapper;


    /**
     * 先查询库存，够的话再减库存
     * -2：库存不存在
     * -1：库存不足
     * >=0：返回扣减后的库存数量，肯定大于等于0的
     */
    public static final String STOCK_REDUCE_LUA=
            "local stock = KEYS[1] " +
                    "local stock_change = tonumber(ARGV[1]) " +
                    "local is_exists = redis.call(\"EXISTS\", stock) " +
                    "if is_exists == 1 then " +
                    "    local stockAftChange = redis.call(\"DECRBY\", stock,stock_change) " +
                    "    if(stockAftChange<0) then " +
                    "        redis.call(\"INCRBY\", stock,stock_change) " +
                    "        return -1 " +
                    "    else  " +
                    "        return stockAftChange " +
                    "    end " +
                    "else " +
                    "    return -2 " +
                    "end";

    /**
     *
     * @Description  扣减库存
     */
    public Object reduceStock(String stockKey,String stockChange){
        Object result  = redisTemplate.execute((RedisCallback<Object>) redisConnection -> {
            Jedis jedis = (Jedis)redisConnection.getNativeConnection();
            return jedis.eval(STOCK_REDUCE_LUA, Collections.unmodifiableList(Arrays.asList(stockKey))
                    ,Collections.unmodifiableList(Arrays.asList(stockChange)));
        });
        return result;
    }

}
