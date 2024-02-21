package com.miyoushe.controller;

import com.cache.util.CacheUtils;
import com.miyoushe.entity.CommonRe;
import com.miyoushe.entity.MiHoYoGachaLinkInfo;
import com.miyoushe.entity.TokenInfo;
import com.miyoushe.service.IMiHoYoApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

    @GetMapping("token")
    public CommonRe<TokenInfo> getToken(@RequestParam("cookie") String cookie) {
        return miHoYoApiService.getToken(cookie);
    }
}
