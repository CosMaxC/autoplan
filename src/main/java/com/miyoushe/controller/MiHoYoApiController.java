package com.miyoushe.controller;

import com.cache.util.CacheUtils;
import com.miyoushe.entity.*;
import com.miyoushe.service.IMiHoYoApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @GetMapping("gacha/links/cookie")
    public CommonRe<MiHoYoGachaLinkInfo> getGachaLink(@RequestParam("cookie") String cookie) {
        return miHoYoApiService.getGachaLinkByCookie(cookie);
    }

    @GetMapping("token")
    public CommonRe<TokenInfo> getToken(@RequestParam("cookie") String cookie) {
        return miHoYoApiService.getToken(cookie);
    }

    @PostMapping("phone/bind")
    public CommonRe<String> bindMobile(@RequestBody BindMobileDto bindMobileDto) {
        // 验证码登录后绑定用户信息
        return miHoYoApiService.bindMobile(bindMobileDto);
    }

    @PostMapping("phone/unbind")
    public CommonRe<String> unbindMobile(@RequestBody UnbindPhoneDto unbindPhoneDto) {
        // 验证码登录后绑定用户信息
        return miHoYoApiService.unbindMobile(unbindPhoneDto);
    }

    @GetMapping("gacha/links/phone")
    public CommonRe<MiHoYoGachaLinkInfo> getGachaLinkByPhone(@RequestParam("phone") String phone) {
        return miHoYoApiService.getGachaLinkByPhone(phone);
    }

    @PostMapping("gacha/links/batch/phone")
    public CommonRe<MiHoYoGachaLinkInfo> getGachaLinkByBatchPhone(@RequestBody List<String> phoneList) {
        return miHoYoApiService.getGachaLinkByBatchPhone(phoneList);
    }

    @GetMapping("gacha/links/stoken")
    public CommonRe<MiHoYoGachaLinkInfo> getGachaLinkByStokenAndStUid(@RequestParam("stoken") String stoken, @RequestParam("stUid") String stUid) {
        return miHoYoApiService.getGachaLinksByStokenAndStUid(stoken, stUid);
    }
}
