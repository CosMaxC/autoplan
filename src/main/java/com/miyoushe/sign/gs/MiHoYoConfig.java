package com.miyoushe.sign.gs;

/**
 * @Author ponking
 * @Date 2021/5/7 14:14
 */
public class MiHoYoConfig {

    /**
     * web端登录获取米游社token相关接口 -- 获取mmt
     */
    public static final String CREATE_MMT_URL = "https://webapi.account.mihoyo.com/Api/create_mmt";
    /**
     * web端登录获取米游社token相关接口 -- 获取验证码
     */
    public static final String CREATE_MOBILE_CAPTCHA_URL = "https://webapi.account.mihoyo.com/Api/create_mobile_captcha";

    /**
     * web端登录获取米游社token相关接口 -- 验证码登录获取login_ticket
     */
    public static final String LOGIN_BY_MOBILE_CAPTCHA_URL = "https://webapi.account.mihoyo.com/Api/login_by_mobilecaptcha";

    /**
     * web端登录获取米游社token相关接口 -- 用cookie登录
     */
    public static final String LOGIN_BY_COOKIE_URL = "https://webapi.account.mihoyo.com/Api/login_by_cookie";

    /**
     * web端登录获取米游社token相关接口 -- 获取用户信息，用于获取cookie_token 和 account_id
     */
    public static final String GET_ACCOUNT_INFO_BY_LOGIN_TICKET_URL = "https://api-takumi.mihoyo.com/account/auth/api/getAccountInfoByLoginTicket";

    /**
     * genshin
     **/
    public static final String GENSHIN_ACT_ID = "e202311201442471"; // 切勿乱修改

    public static final String STAR_RAIL_ACT_ID = "e202304121516551"; // 切勿乱修改

    public static final String APP_VERSION = "2.3.0"; // 切勿乱修改

    public static final String NEW_SIGN_ORIGIN = "https://act.mihoyo.com/";

    /**
     * Android 2
     */
    public static final String CLIENT_TYPE = "5"; // 切勿乱修改

    public static final String DEVICE_NAME = "Xiaomi Redmi Note 4";

    public static final String DEVICE_MODE = "Redmi Note 4";

    public static final String REGION = "cn_gf01"; // 切勿乱修改

    public static final String MYS_PERSONAL_INFO_URL = "https://bbs-api.miyoushe.com/user/wapi/getUserFullInfo?gids=2";

    /**
     * 原神抽卡url
     */
    public static final String GENSHIN_GACHA_URL = "https://hk4e-api.mihoyo.com/event/gacha_info/api/getGachaLog?authkey_ver=1&lang=zh-cn&authkey={authkey}&game_biz={game_biz}&gacha_type=301";
    /**
     * 崩铁抽卡url
     */
    public static final String STAR_RAIL_GACHA_URL= "https://api-takumi.mihoyo.com/common/gacha_record/api/getGachaLog?authkey_ver=1&lang=zh-cn&authkey={authkey}&game_biz={game_biz}&gacha_type=11";
    /**
     * 用login_ticket 获取stoken
     */
    public static final String MYS_TOKEN_URL = "https://api-takumi.miyoushe.com/auth/api/getMultiTokenByLoginTicket?login_ticket=%s&token_types=3&uid=%s";
    /**
     * 用stoken获取抽卡需要的authKey
     */
    public static final String GET_AUTH_KEY_URL = "https://api-takumi.mihoyo.com/binding/api/genAuthKey";
    /**
     * stoken获取cookie_token
     */
    public static final String GET_COOKIE_TOKEN_BY_STOKEN_URL = "https://passport-api.mihoyo.com/account/auth/api/getCookieAccountInfoBySToken";
    /**
     * stoken获取ltoken
     */
    public static final String GET_LTOKEN_BY_STOKEN_URL = "https://passport-api.mihoyo.com/account/auth/api/getLTokenBySToken";
    /**
     * stoken获取stoken_v2
     */
    public static final String GET_STOKEN_V2_BY_V1_URL = "https://passport-api.mihoyo.com/account/ma-cn-session/app/getTokenBySToken";
    /**
     * stoken获取ticket（非login_ticket）
     */
    public static final String ACTION_TICKET_URL = "https://api-takumi.mihoyo.com/auth/api/getActionTicketBySToken?action_type=game_role&stoken=%s&uid=%s";


    public static final String GENSHIN_REFERER_URL = String.format("https://webstatic.mihoyo.com/bbs/event/signin-ys/index.html?bbs_auth_required=%s&act_id=%s&utm_source=%s&utm_medium=%s&utm_campaign=%s", true, GENSHIN_ACT_ID, "bbs", "mys", "icon");

    public static final String STAR_RAIL_REFERER_URL = String.format("https://webstatic.mihoyo.com/bbs/event/signin-ys/index.html?bbs_auth_required=%s&act_id=%s&utm_source=%s&utm_medium=%s&utm_campaign=%s", true, STAR_RAIL_ACT_ID, "bbs", "mys", "icon");

    public static final String GENSHIN_AWARD_URL = String.format("https://api-takumi.mihoyo.com/event/luna/home?act_id=%s", GENSHIN_ACT_ID);

    public static final String STAR_RAIL_AWARD_URL = String.format("https://api-takumi.mihoyo.com/event/luna/home?act_id=%s", STAR_RAIL_ACT_ID);

    public static final String GENSHIN_ROLE_URL = String.format("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=%s", "hk4e_cn");

     static final String STAR_RAIL_ROLE_URL = String.format("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=%s", "hkrpg_cn");

    public static final String INFO_URL = "https://api-takumi.mihoyo.com/event/luna/info";

    public static final String SIGN_URL = "https://api-takumi.mihoyo.com/event/luna/sign";

    public static final String USER_AGENT = String.format("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) miHoYoBBS/%s", APP_VERSION);

    public static final String USER_AGENT_TEMPLATE = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) miHoYoBBS/%s";

//    public static final String USER_AGENT = "okhttp/4.8.0";


    /**
     * mihoyo hub
     */
    public static final String SIGN_IN_STATUS = "https://bbs-api.mihoyo.com/apihub/sapi/querySignInStatus";

    public static final String HUB_COOKIE1_URL = "https://webapi.account.mihoyo.com/Api/cookie_accountinfo_by_loginticket";

    public static final String HUB_COOKIE2_URL = "https://api-takumi.mihoyo.com/auth/api/getMultiTokenByLoginTicket?login_ticket=%s&token_types=3&uid=%s";

    //public static final String HUB_SIGN_URL = "https://bbs-api.mihoyo.com/apihub/sapi/signIn?gids=%s";
    public static final String HUB_SIGN_URL = "https://bbs-api.mihoyo.com/apihub/app/api/signIn";

    public static final String HUB_LIST1_URL = "https://bbs-api.mihoyo.com/post/api/getForumPostList?forum_id=%s&is_good=false&is_hot=false&page_size=20&sort_type=1";

    public static final String HUB_LIST2_URL = "https://bbs-api.mihoyo.com/post/api/feeds/posts?fresh_action=1&gids=%s&last_id=";

    public static final String HUB_VIEW_URL = "https://bbs-api.mihoyo.com/post/api/getPostFull?post_id=%s";

    public static final String HUB_SHARE_URL = "https://bbs-api.mihoyo.com/apihub/api/getShareConf?entity_id=%s&entity_type=1";

    public static final String HUB_EXTERNAL_LINK_URL = "https://bbs-api.mihoyo.com/post/api/externalLink?post_id=%s";

    public static final String HUB_VOTE_URL = "https://bbs-api.mihoyo.com/apihub/sapi/upvotePost";

    public static final String BBS_GET_CAPTCHA_URL = "https://bbs-api.mihoyo.com/misc/api/createVerification?is_high=true";

    public static final String BBS_CAPTCHA_VERIFY_URL  = "https://bbs-api.mihoyo.com/misc/api/verifyVerification";


    public enum HubsEnum {
        BH3(new Hub.Builder().setId("1").setForumId("1").setName("崩坏3").setUrl("https://bbs.mihoyo.com/bh3/").build()),
//        YS(new Hub.Builder().setId("2").setForumId("2").setName("原神").setUrl("https://bbs.mihoyo.com/ys/").build()),
        YS(new Hub.Builder().setId("2").setForumId("26").setName("原神").setUrl("https://bbs.mihoyo.com/ys/").build()),
        BH2(new Hub.Builder().setId("3").setForumId("30").setName("崩坏2").setUrl("https://bbs.mihoyo.com/bh2/").build()),
        WD(new Hub.Builder().setId("4").setForumId("37").setName("未定事件簿").setUrl("https://bbs.mihoyo.com/wd/").build()),
        DBY(new Hub.Builder().setId("5").setForumId("34").setName("大别野").setUrl("https://bbs.mihoyo.com/dby/").build());

        private Hub game;

        HubsEnum(Hub game) {
            this.game = game;
        }

        public Hub getGame() {
            return game;
        }
    }


    public static class Hub {

        private String id;
        private String forumId;
        private String name;
        private String url;

        public Hub() {
        }

        private Hub(Builder builder) {
            this.id = builder.id;
            this.forumId = builder.forumId;
            this.name = builder.name;
            this.url = builder.url;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getForumId() {
            return forumId;
        }

        public void setForumId(String forumId) {
            this.forumId = forumId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public static class Builder {

            private String id;
            private String forumId;
            private String name;
            private String url;

            public Builder setId(String id) {
                this.id = id;
                return this;
            }

            public Builder setForumId(String forumId) {
                this.forumId = forumId;
                return this;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setUrl(String url) {
                this.url = url;
                return this;
            }

            public Hub build() {
                return new Hub(this);
            }
        }
    }

    public static class DeviceConfig {
        /**
         * 移动端 User-Agent(Mozilla UA)
         */
        public static final String USER_AGENT_MOBILE = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) miHoYoBBS/2.55.1";
        /**
         * 桌面端 User-Agent(Mozilla UA)
         */
        public static final String USER_AGENT_PC = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Safari/605.1.15";
        /**
         * 获取用户 ActionTicket 时Headers所用的 User-Agent
         */
        public static final String USER_AGENT_OTHER = "Hyperion/275 CFNetwork/1402.0.8 Darwin/22.2.0";
        /**
         * 安卓端 User-Agent(Mozilla UA)
         */
        public static final String USER_AGENT_ANDROID = "Mozilla/5.0 (Linux; Android 11; MI 8 SE Build/RQ3A.211001.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/104.0.5112.97 Mobile Safari/537.36 miHoYoBBS/2.55.1";
        /**
         * 安卓端 User-Agent(专用于米游币任务等)
         */
        public static final String USER_AGENT_ANDROID_OTHER = "okhttp/4.9.3";
        /**
         * iOS 小组件 User-Agent(原神实时便笺)
         */
        public static final String USER_AGENT_WIDGET = "WidgetExtension/231 CFNetwork/1390 Darwin/22.0.0";



        /**
         * 移动端 x-rpc-device_model
         */
        public static final String X_RPC_DEVICE_MODEL_MOBILE = "iPhone10,2";
        /**
         * 桌面端 x-rpc-device_model
         */
        public static final String X_RPC_DEVICE_MODEL_PC = "OS X 10.15.7";
        /**
         * 安卓端 x-rpc-device_model
         */
        public static final String X_RPC_DEVICE_MODEL_ANDROID = "MI 8 SE";



        /**
         * 移动端 x-rpc-device_name
         */
        public static final String X_RPC_DEVICE_NAME_MOBILE = "iPhone";
        /**
         * 桌面端 x-rpc-device_name
         */
        public static final String X_RPC_DEVICE_NAME_PC = "Microsoft Edge 103.0.1264.62";
        /**
         * 安卓端 x-rpc-device_name
         */
        public static final String X_RPC_DEVICE_NAME_ANDROID = "Xiaomi MI 8 SE";


        /**
         * Headers所用的 x-rpc-sys_version
         */
        public static final String X_RPC_SYS_VERSION = "16.2";
        /**
         * 安卓端 x-rpc-sys_version
         */
        public static final String X_RPC_SYS_VERSION_ANDROID = "11";


        /**
         * Headers所用的 x-rpc-channel
         */
        public static final String X_RPC_CHANNEL = "appstore";
        /**
         * 安卓端 x-rpc-channel
         */
        public static final String X_RPC_CHANNEL_ANDROID = "miyousheluodi";


        /**
         * Headers所用的 x-rpc-app_version
         */
        public static final String X_RPC_APP_VERSION = "2.63.1";
        /**
         * Headers所用的 x-rpc-platform
         */
        public static final String X_RPC_PLATFORM = "ios";
        /**
         * Headers所用的 sec-ch-ua
         */
        public static final String SEC_CH_UA = "\".Not/A)Brand\";v=\"99\", \"Microsoft Edge\";v=\"103\", \"Chromium\";v=\"103\"";
        /**
         * Headers所用的 sec-ch-ua-platform
         */
        public static final String SEC_CH_UA_PLATFORM = "\"macOS\"";
    }
}
