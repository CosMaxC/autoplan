package com.miyoushe.entity;

import lombok.Data;

/**
 * @author :
 * @date :2024/2/21 11:24
 * @description :
 */

@Data
public class TokenInfo {

    /**
     * stoken
     */
    private String stoken;
    /**
     * stoken_v2
     */
    private StokenV2Info stokenV2Info;

    /**
     * stuid
     */
    private String stuid;

    /**
     * ltoken
     */
    private String lToken;

    /**
     * 登录ticket
     */
    private String loginTicket;
    /**
     * 登录uid
     */
    private String loginUid;

    /**
     * loginTicke和loginUid获取的cookie_token
     */
    private String cookieToken;
}
