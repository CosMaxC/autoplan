package com.miyoushe.entity;

import lombok.Data;

import java.util.List;

/**
 * @author :
 * @date :2024/2/20 11:34
 * @description :
 */

@Data
public class MiHoYoGachaLinkInfo {

    /**
     * 原神抽卡link
     */
    private List<LinkInfo> genshinLink;

    /**
     * 崩铁抽卡link
     */
    private List<LinkInfo> starLink;

    @Data
    public static class LinkInfo {

        /**
         * 对应uid
         */
        private String uid;

        /**
         * 对应名称
         */
        private String nickname;
        /**
         * 对应url
         */
        private String url;
    }
}
