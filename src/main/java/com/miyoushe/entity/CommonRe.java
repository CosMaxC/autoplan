package com.miyoushe.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class CommonRe<T> {

    /**
     * 状态码 200-正常 400-错误请求 403-拒绝请求 500-服务器内部错误 503-服务不可用
     */
    private Integer status;
    /**
     * 自定义错误码
     */
    private String errCode;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 提示语
     */
    private String msg;

    public CommonRe(Integer status, String errCode, T data, String msg) {
        this.status = status;
        this.errCode = errCode;
        this.data = data;
        this.msg = msg;
    }

    public static <T> CommonRe<T> success(T data) {
        return success(data, null);
    }

    public static <T> CommonRe<T> success(T data, String msg) {
        return new CommonRe<>(HttpStatus.OK.value(), null, data, msg);
    }

    public static <T> CommonRe<T> error(String msg) {
        return error(HttpStatus.BAD_REQUEST.value(), null, msg);
    }

    public static <T> CommonRe<T> error(Integer status, String msg) {
        return error(status, null, msg);
    }

    public static <T> CommonRe<T> error(String errCode, String msg) {
        return error(HttpStatus.BAD_REQUEST.value(), errCode, msg);
    }

    public static <T> CommonRe<T> error(String errCode, String msg, Object... args) {
        return error(HttpStatus.BAD_REQUEST.value(), errCode, msg);
    }

    public static <T> CommonRe<T> error(Integer status, String errCode, String msg, Object... args) {
        return new CommonRe<>(status, errCode, null, msg);
    }


    public boolean isSuccess() {
        return status == 200;
    }
}
