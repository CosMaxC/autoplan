package com.push.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.push.AbstractPush;
import com.push.model.PushMetaInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author :
 * @date :2024/3/1 17:34
 * @description :
 */

public class CustomizePush extends AbstractPush {

    private String url;
    private String token;

    public CustomizePush(String url, String token) {
        this.token = token;
        this.url = url;
    }

    /**
     * 生成推送URL
     *
     * @param metaInfo 元信息
     * @return URL字符串
     */
    @Override
    protected String generatePushUrl(PushMetaInfo metaInfo) {
        return url;
    }

    /**
     * 检查推送结果
     *
     * @param jsonObject HTTP结果，可能为<code>null</code>
     * @return 推送成功，返回<code>true</code>
     */
    @Override
    protected boolean checkPushStatus(JSONObject jsonObject) {
        if (jsonObject == null) {
            return false;
        }
        Integer status = jsonObject.getInteger("status");
        return status != null && status.equals(200);
    }

    /**
     * 生成要推送的内容信息
     *
     * @param metaInfo 元信息
     * @param content  要推送的内容
     * @return 整理后的推送内容
     */
    @Override
    protected String generatePushBody(PushMetaInfo metaInfo, String content) {
        Map<String, String> map = new HashMap<>(2);
        map.put("token", token);
        map.put("content", content);
        return JSONUtil.toJsonStr(map);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
