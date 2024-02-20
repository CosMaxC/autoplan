package com.captcha.util;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CaptchaUtil {

    public static final String RR_OCR_URL = "http://api.rrocr.com/api/recognize.html";

    @Value("${ocr.app-key}")
    private String appKey;

    private static String ocrKey;

    @PostConstruct
    public void setKey() {
        ocrKey = appKey;
    }

    /**
     * 用于v3校验
     * @param gt 验证码所在网站极验的gt
     * @param challenge 通过请求目标站配置项获取，每次获取到的challenge仅能使用一次。如网站为极验感知无此参数可不填，如果是四代的话challenge无需传
     * @param referer 验证码所在的页面URL
     */
    public static Triple<Boolean, String, String> getValidateByRrOcr(String gt, String challenge, String referer) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("User-Agent", "Mozilla/5.0 Chrome/77.0.3865.120 Safari/537.36");
        headerMap.put("Content-Type", "application/x-www-form-urlencoded;");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gt", gt);
        paramMap.put("appkey", ocrKey);
        paramMap.put("challenge", challenge);
        paramMap.put("referer", referer);
        HttpResponse execute = HttpUtil.createPost(RR_OCR_URL).addHeaders(headerMap).setConnectionTimeout(60 * 1000).form(paramMap).execute();
        String body = execute.body();
        log.info("验证码返回：{}", body);
        JSONObject jsonObject = JSONUtil.parseObj(body);
        if (jsonObject.getInt("status") != 0) {
            return ImmutableTriple.of(false, jsonObject.getStr("msg"), null);
        }
        JSONObject data = jsonObject.getJSONObject("data");
        String validate = data.getStr("validate");
        return ImmutableTriple.of(true, null, validate);
    }

    /**
     * 用于v4
     * @param gt 验证码所在网站极验的gt值，如果是四代的话gt参数为captcha_id值
     * @param mmtKey 特定网站所需的参数，四代个别定制板如有此参数需要传此参数，如定制版包里有mmt_key需要传此参数
     * @param referer 验证码所在的页面URL
     */
    public static Triple<Boolean, String, String> getValidateV4ByRrOcr(String gt, String mmtKey, String referer) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("User-Agent", "Mozilla/5.0 Chrome/77.0.3865.120 Safari/537.36");
        headerMap.put("Content-Type", "application/x-www-form-urlencoded;");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gt", gt);
        paramMap.put("appkey", ocrKey);
        paramMap.put("mmt_key", mmtKey);
        paramMap.put("referer", referer);
        HttpResponse execute = HttpUtil.createPost(RR_OCR_URL).addHeaders(headerMap).setConnectionTimeout(60 * 1000).form(paramMap).execute();
        String body = execute.body();
        log.info("验证码返回：{}", body);
        JSONObject jsonObject = JSONUtil.parseObj(body);
        if (jsonObject.getInt("status") != 0) {
            return ImmutableTriple.of(false, jsonObject.getStr("msg"), null);
        }
        JSONObject data = jsonObject.getJSONObject("data");
        String secCode = data.getStr("seccode");
        return ImmutableTriple.of(true, null, secCode);
    }
}
