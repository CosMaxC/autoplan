package com.miyoushe.entity;

import lombok.Data;

/**
 * @author :
 * @date :2024/3/2 19:37
 * @description :
 */

@Data
public class BindMobileDto {

    /**
     * 绑定手机号
     */
    private String mobile;
    /**
     * 绑定账号
     */
    private String account;
    /**
     * 验证码
     */
    private String captcha;
    /**
     * webhook token
     */
    private String webHookToken;
    /**
     * webhook url
     */
    private String webHookUrl;
}
