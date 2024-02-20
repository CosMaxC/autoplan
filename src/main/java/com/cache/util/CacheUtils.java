package com.cache.util;

import cn.hutool.core.lang.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author :
 * @date :2024/2/19 9:47
 * @description :
 */

@Component
public class CacheUtils {

    @Cacheable(value = "deviceCache=1", key = "#id")
    public String getDeviceId(String id) {
        return UUID.randomUUID().toString();
    }
}
