package com.miyoushe.entity;

import lombok.Data;

import java.util.List;

/**
 * @author :
 * @date :2024/3/1 16:40
 * @description :
 */

@Data
public class UnbindPhoneDto {

    /**
     * 账号
     */
    private String account;

    /**
     * 手机号
     */
    private List<String> mobileList;
}
