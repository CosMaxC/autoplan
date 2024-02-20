package com.miyoushe.service;

import com.miyoushe.entity.CommonRe;
import com.miyoushe.entity.MiHoYoGachaLinkInfo;

/**
 * @author :
 * @date :2024/2/19 10:06
 * @description :
 */
public interface IMiHoYoApiService {
    /**
     * 获取验证码
     * @param phone 手机号
     * @return 结果
     */
    CommonRe<String> getCaptcha(String phone);

    /**
     * 验证码登录
     * @param phone 手机号
     * @param captcha 验证码
     * @return 结果
     */
    CommonRe<String> captchaLogin(String phone, String captcha);

    /**
     * 通过cookie 或者stoken获取抽卡url
     * @param cookie cookie
     * @return 结果
     */
    CommonRe<MiHoYoGachaLinkInfo> getGachaLinkByCookie(String cookie);
}
