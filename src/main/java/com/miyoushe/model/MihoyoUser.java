package com.miyoushe.model;

import java.util.Date;
import lombok.Data;

@Data
public class MihoyoUser {
    /**
    * id
    */
    private Integer id;

    /**
    * 手机号
    */
    private String mobile;

    /**
    * 米哈游uid
    */
    private String uid;

    /**
    * 米哈游stoken_v1
    */
    private String stokenV1;

    /**
    * 米哈游ltoken
    */
    private String ltoken;

    /**
    * 米哈游cookie_token
    */
    private String cookieToken;

    /**
    * 米哈游stoken_v2
    */
    private String stokenV2;

    /**
    * 米哈游mid，与stoken_v2一起使用
    */
    private String mid;

    private Date createTime;

    private Date updateTime;
}