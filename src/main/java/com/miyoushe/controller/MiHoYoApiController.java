package com.miyoushe.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.cache.util.CacheUtils;
import com.captcha.util.CaptchaUtil;
import com.miyoushe.entity.CommonRe;
import com.miyoushe.entity.MiHoYoGachaLinkInfo;
import com.miyoushe.service.IMiHoYoApiService;
import com.miyoushe.sign.gs.MiHoYoAbstractSign;
import com.miyoushe.sign.gs.MiHoYoConfig;
import com.miyoushe.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.Header;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author :
 * @date :2024/2/16 22:14
 * @description :
 */
@Slf4j
@RestController
@RequestMapping("api/mihoyo")
public class MiHoYoApiController {

    @Resource
    private CacheUtils cacheUtils;

    @Resource
    private IMiHoYoApiService miHoYoApiService;

    @GetMapping("captcha/access")
    public CommonRe<String> getCaptcha(@RequestParam("phone") String phone) {
        return miHoYoApiService.getCaptcha(phone);

    }

    @GetMapping("captcha/login")
    public CommonRe<String> captchaLogin(@RequestParam("phone") String phone, @RequestParam("captcha") String captcha) {
        return miHoYoApiService.captchaLogin(phone, captcha);
    }

    @GetMapping("captcha/cache")
    public String testCache(@RequestParam("phone") String phone) {
        return cacheUtils.getDeviceId(phone);
    }

    @GetMapping("gacha/links")
    public CommonRe<MiHoYoGachaLinkInfo> getGachaLink(@RequestParam("cookie") String cookie) {
        return miHoYoApiService.getGachaLinkByCookie(cookie);
    }
}
