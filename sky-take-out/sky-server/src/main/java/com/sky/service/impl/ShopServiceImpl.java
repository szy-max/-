package com.sky.service.impl;

import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {

   @Autowired

   private RedisTemplate<String, Object> redisTemplate;


    @Override
    public Integer getStatus() {
        return (Integer) redisTemplate.opsForValue().get("shop_status");
    }

    @Override
    public void setStatus(Integer status) {
        redisTemplate.opsForValue().set("shop_status",status);
    }
}
