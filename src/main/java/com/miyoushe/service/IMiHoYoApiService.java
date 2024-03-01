package com.miyoushe.service;

import com.miyoushe.entity.CommonRe;
import com.miyoushe.entity.MiHoYoGachaLinkInfo;
import com.miyoushe.entity.TokenInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

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

    /**
     * 获取stoken
     * @param cookie cookie
     * @return stoken
     */
    CommonRe<TokenInfo> getToken(String cookie);

    /**
     * 绑定手机号和uid
     * @param phone 手机号
     * @param captcha 验证码
     * @return 结果
     */
    CommonRe<String> bindMobile(String phone, String captcha);

    Triple<Boolean, String, String> getCookieTokenByStoken(String stoken, String stuid);

    /**
     * 根据手机号获取抽卡url
     * @param phone 手机号
     * @return 结果
     */
    CommonRe<MiHoYoGachaLinkInfo> getGachaLinkByPhone(String phone);

    CommonRe<MiHoYoGachaLinkInfo>  getGachaLinksByStokenAndStUid(String stokenV1, String uid);

    /**
     * 批量通过手机号获取抽卡url
     * @param phoneList 手机号list
     * @return 结果
     */
    CommonRe<MiHoYoGachaLinkInfo> getGachaLinkByBatchPhone(List<String> phoneList);
}
