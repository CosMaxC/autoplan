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
    * 米哈游suid
    */
    private String suid;

    private Date createTime;

    private Date updateTime;
}