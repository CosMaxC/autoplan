package com.cache;

import cn.hutool.core.convert.Convert;
import com.google.common.cache.CacheBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CustomCacheManager extends CachingConfigurerSupport {

    @Override
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(final String name) {
                String cacheName;
                long duration;
                if (name.contains("=")) {
                    String[] split = name.split("=");
                    cacheName = split[0];
                    duration = Convert.toLong(split[1]);
                } else {
                    cacheName = name;
                    duration = 10;
                }
                return new ConcurrentMapCache(cacheName, // 缓存的名称
                        CacheBuilder.newBuilder()
                                .expireAfterWrite(duration, TimeUnit.MINUTES) // 指定缓存过期时间
                                .maximumSize(10000) // 指定缓存的最大大小为10000条记录
                                .build()
                                .asMap(),
                        false);
            }
        };
    }

}