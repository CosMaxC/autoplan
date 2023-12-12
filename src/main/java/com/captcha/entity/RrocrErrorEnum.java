package com.captcha.entity;


import lombok.Getter;

@Getter
public enum RrocrErrorEnum {
    IP_ERROR(1001, "IP代理网络错误"),
    IP_FORBID_ERROR(1011, "网络异常，IP被封"),
    GET_CHALLENGE_ERROR(1002, "获取challenge错误 请检查提交的challenge是不是极验网站的"),
    INTERNAL_ERROR(1022, "内部错误"),
    LOCATION_ERROR(1003, "坐标识别错误"),
    TRACE_ERROR(1033, "轨迹错误"),
    INIT_CHALLENGE_ERROR(1004, "识别初始化错误 请检查是不是初始页面得来的challenge 长度为32位且只提交过平台一次且不是从抓包得来的"),
    INIT_CHALLENGE_ONE_ERROR(1044, "识别初始化错误1 请检查是不是初始页面得来的challenge 长度为32位且只提交过平台一次且不是从抓包得来的"),
    REQUEST_PARAM_ERROR(1005, "获取必须参数失败"),
    BUG_WEBSITE_ERROR(1055, "屏蔽bug网站"),
    CHECK_CHALLENGE_ERROR(108, "challenge参数有错误不是极验的或者请检查提交是否为三代类型并且检查参数challenge是否为空");

    /**
     * 错误码
     */
    private final int code;
    /**
     * 错误信息
     */
    private final String msg;

    RrocrErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
