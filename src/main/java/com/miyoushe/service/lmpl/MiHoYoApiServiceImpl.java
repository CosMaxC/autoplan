package com.miyoushe.service.lmpl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cache.util.CacheUtils;
import com.captcha.util.CaptchaUtil;
import com.miyoushe.entity.CommonRe;
import com.miyoushe.entity.MiHoYoGachaLinkInfo;
import com.miyoushe.mapper.MihoyoUserMapper;
import com.miyoushe.service.IMiHoYoApiService;
import com.miyoushe.service.MihayouService;
import com.miyoushe.sign.gs.GenShinSignMiHoYo;
import com.miyoushe.sign.gs.MiHoYoAbstractSign;
import com.miyoushe.sign.gs.MiHoYoConfig;
import com.miyoushe.sign.gs.StarRailSignMiHoYo;
import com.miyoushe.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.Header;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.HttpCookie;
import java.util.*;

/**
 * @author :
 * @date :2024/2/19 10:06
 * @description :
 */

@Slf4j
@Service
public class MiHoYoApiServiceImpl implements IMiHoYoApiService {

    @Resource
    private CacheUtils cacheUtils;

    @Resource
    private MihoyoUserMapper mihoyoUserMapper;

    @Resource
    private MihayouService mihayouService;
    /**
     * 获取验证码
     *
     * @param phone 手机号
     * @return 结果
     */
    @Override
    public CommonRe<String> getCaptcha(String phone) {
        // 1. 获取mmt_key
        Map<String, String> commonHeaderMap = getCaptchaHeaderMap();
        commonHeaderMap.put("x-rpc-device_id", cacheUtils.getDeviceId(phone));
        commonHeaderMap.put("x-rpc-source", "accountWebsite");
        Triple<Boolean, String, JSONObject> getMmtResult = getMmtKey(commonHeaderMap);

        if (!getMmtResult.getLeft()) {
            return CommonRe.error(getMmtResult.getMiddle());
        }
        JSONObject mmtData = getMmtResult.getRight();

        // 2. 封装发送code参数，并通过极验
        Triple<Boolean, String, Map<String, Object>> generateSendCodeMapResult = generateSendCodeMap(commonHeaderMap, mmtData, phone);
        if (!generateSendCodeMapResult.getLeft()) {
            return CommonRe.error(generateSendCodeMapResult.getMiddle());
        }

        // 3. 手机加gt获取验证码
        Map<String, Object> sendMobileCodeMap = generateSendCodeMapResult.getRight();
        Pair<Boolean, String> sendMobileCodeResult = sendMobileCode(commonHeaderMap, sendMobileCodeMap);
        if (sendMobileCodeResult.getLeft()) {
            return CommonRe.success(null);
        } else {
            return CommonRe.error(sendMobileCodeResult.getRight());
        }

    }

    /**
     * 验证码登录
     *
     * @param phone   手机号
     * @param captcha 验证码
     * @return 结果
     */
    @Override
    public CommonRe<String> captchaLogin(String phone, String captcha) {
        Map<String, String> commonHeaderMap = getCaptchaHeaderMap();
        String deviceId = cacheUtils.getDeviceId(phone);
        commonHeaderMap.put("x-rpc-device_id", deviceId);
        commonHeaderMap.put("x-rpc-source", "accountWebsite");
        // 1. 登录获取token

        Triple<Boolean, String, String> loginAndGetCookieResult = loginAndGetCookie(commonHeaderMap, phone, captcha);
        if (!loginAndGetCookieResult.getLeft()) {
            return CommonRe.error(loginAndGetCookieResult.getMiddle());
        }

        // 用cookie登录
        Pair<Boolean, String> loginByCookieResult = loginByCookie(commonHeaderMap);
        if (!loginByCookieResult.getLeft()) {
            return CommonRe.error(loginByCookieResult.getRight());
        }

        // 用cookie获取cookie_token和account_id，拼接生成最终cookie
        Map<String, String> cookieMap = getFinalCookieMap(deviceId, loginAndGetCookieResult.getRight());

        Triple<Boolean, String, String> getFinalCookieResult = getFinalCookie(cookieMap);
        if (!getFinalCookieResult.getLeft()) {
            return CommonRe.error(getFinalCookieResult.getMiddle());
        }

        return CommonRe.success(getFinalCookieResult.getRight());
    }

    @Override
    public CommonRe<MiHoYoGachaLinkInfo> getGachaLinkByCookie(String cookie) {
        if (StrUtil.isBlank(cookie)) {
            return CommonRe.error("cookie不能为空");
        }

        MiHoYoGachaLinkInfo miHoYoGachaLinkInfo = new MiHoYoGachaLinkInfo();
        // genshin抽卡url
        GenShinSignMiHoYo genShinSignMiHoYo = new GenShinSignMiHoYo(cookie);
        List<Map<String, Object>> genShinUidInfos = genShinSignMiHoYo.getUid();
        Header[] genshinHeaders = genShinSignMiHoYo.getBasicHeaders();

        List<MiHoYoGachaLinkInfo.LinkInfo> genshinLink = getCommonGachaLinks(genShinUidInfos, cookie, MiHoYoConfig.GENSHIN_GACHA_URL);
        // starRail抽卡url
        StarRailSignMiHoYo starRailSignMiHoYo = new StarRailSignMiHoYo(cookie);
        List<Map<String, Object>> starRailUidInfos = starRailSignMiHoYo.getUid();
        Header[] starRailHeaders = starRailSignMiHoYo.getBasicHeaders();
        List<MiHoYoGachaLinkInfo.LinkInfo> starRailLink = getCommonGachaLinks(starRailUidInfos, cookie, MiHoYoConfig.STAR_RAIL_GACHA_URL);
        miHoYoGachaLinkInfo.setStarLink(starRailLink);
        miHoYoGachaLinkInfo.setGenshinLink(genshinLink);

        return CommonRe.success(miHoYoGachaLinkInfo);
    }

    /**
     * 获取genshin抽卡url
     * @param uidInfos uid信息
     * @param cookie cookie
     * @param gachaUrl 抽卡url
     * @return 抽卡url
     */
    private List<MiHoYoGachaLinkInfo.LinkInfo> getCommonGachaLinks(List<Map<String, Object>> uidInfos, String cookie, String gachaUrl) {



        List<MiHoYoGachaLinkInfo.LinkInfo> linkInfos = new ArrayList<>();
        for (Map<String, Object> mapInfo : uidInfos) {
            String loginTicket = com.oldwu.util.HttpUtils.getCookieByName(cookie, "login_ticket");
            String loginUid = com.oldwu.util.HttpUtils.getCookieByName(cookie, "login_uid");
            Map<String, Object> cookieTokenResult = mihayouService.getCookieToken(loginTicket, loginUid);
            boolean isSuccess = Convert.toBool(cookieTokenResult.get("flag"));
            String getTokenMsg = Convert.toStr(cookieTokenResult.get("msg"));
            if (!isSuccess) {
                String msg = "获取抽卡URL：获取stoken失败：" + getTokenMsg;
                log.error(msg);
                continue;
            }
            String stoken = Convert.toStr(cookieTokenResult.get("stoken"));
            String stuid = loginUid;
            Map<String, String> headerMap = getAuthKeyHeader(stoken, stuid);

            boolean flag = Convert.toBool(mapInfo.get("flag"));
            String mapInfoMsg = Convert.toStr(mapInfo.get("msg"));
            if (!flag) {
                String msg = "读取mapInfo失败：" + mapInfoMsg;
                log.warn(msg);
                continue;
            }

            String uid = Convert.toStr(mapInfo.get("uid"));
            String nickname = Convert.toStr(mapInfo.get("nickname"));
            String gameBiz = Convert.toStr(mapInfo.get("game_biz"));
            Triple<Boolean, String, String> getSingleGachaLinkResult = getSingleGachaLink(headerMap, gameBiz, mapInfo, gachaUrl);
            if (!getSingleGachaLinkResult.getLeft()) {
                log.warn("获取抽卡url失败：[{}--{}]:{}", uid, nickname, getSingleGachaLinkResult.getMiddle());
                continue;
            }
            String url = getSingleGachaLinkResult.getRight();
            MiHoYoGachaLinkInfo.LinkInfo linkInfo = new MiHoYoGachaLinkInfo.LinkInfo();
            linkInfo.setUid(uid);
            linkInfo.setNickname(nickname);
            linkInfo.setUrl(url);
            linkInfos.add(linkInfo);
        }
        return linkInfos;
    }

    /**
     * 获取抽卡url
     *
     * @param headerMap 请求头
     * @param gameBiz   游戏服
     * @param mapInfo   单个账号参数
     * @param gachaUrl 抽卡url
     * @return 结果
     */
    private Triple<Boolean, String, String> getSingleGachaLink(Map<String, String> headerMap, String gameBiz, Map<String, Object> mapInfo, String gachaUrl) {
        String uid = Convert.toStr(mapInfo.get("uid"));
        String nickname = Convert.toStr(mapInfo.get("nickname"));
        String region = Convert.toStr(mapInfo.get("region"));

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("auth_appid", "webview_gacha");
        jsonMap.put("game_biz", gameBiz);
        jsonMap.put("game_uid", uid);
        jsonMap.put("region", region);
        HttpResponse execute = HttpUtil.createPost(MiHoYoConfig.GET_AUTH_KEY_URL).addHeaders(headerMap).body(JSONUtil.toJsonStr(jsonMap)).execute();
        if (execute.getStatus() != 200) {
            String msg = "用户["+ uid +"---"+ nickname +"]读取失败";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }

        String body = execute.body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        if (jsonObject == null) {
            String msg = "获取authKey失败：返回json为空";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        if (jsonObject.getIntValue("retcode") != 0) {
            String msg = "获取authKey失败：["+ jsonObject.getIntValue("retcode") +"]:" + jsonObject.getString("message");
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        log.info("返回结果：{}", body);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) {
            String msg = "获取authKey失败：返回data为空";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        String authkey = data.getString("authkey");
        if (StrUtil.isBlank(authkey)) {
            String msg = "获取authKey失败：authKey为空";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        String url = getGachaLink(authkey, gameBiz, gachaUrl);
        return ImmutableTriple.of(true, "成功", url);
    }

    /**
     * 获取抽卡地址
     *
     * @param authkey  读取抽卡的key
     * @param gameBiz  游戏服务器
     * @param gachaUrl 抽卡url
     * @return url
     */
    private String getGachaLink(String authkey, String gameBiz, String gachaUrl) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("authkey", URLEncodeUtil.encodeAll(authkey));
        paramMap.put("game_biz", gameBiz);
        return StrUtil.format(gachaUrl, paramMap);
    }

    /**
     * 根据stoken生成获取authKey请求头
     *
     * @param stoken       stoken
     * @param stuid         stuid
     * @return 请求头
     */
    private Map<String, String> getAuthKeyHeader(String stoken, String stuid) {
        Map<String, String> headerMap = new HashMap<>();
        String cookie = String.format("stuid=%s; stoken=%s", stuid, stoken);
        headerMap.put("Cookie", cookie);
        headerMap.put("DS", getAuthKeyDS());
        headerMap.put("User-Agent", "okhttp/4.8.0");
        headerMap.put("x-rpc-app_version", "2.49.1");
        headerMap.put("x-rpc-sys_version", "12");
        headerMap.put("x-rpc-client_type", "5");
        headerMap.put("x-rpc-channel", "mihoyo");
        headerMap.put("x-rpc-device_id", UUID.randomUUID().toString());
        headerMap.put("x-rpc-device_name", MiHoYoConfig.DeviceConfig.X_RPC_DEVICE_NAME_PC);
        headerMap.put("x-rpc-device_model", "Mi 10");
        headerMap.put("Referer", "https://app.mihoyo.com");
        headerMap.put("Host", "api-takumi.mihoyo.com");
        return headerMap;
    }

    /**
     * 获取ds
     */
    private String getAuthKeyDS() {
        String i = (System.currentTimeMillis() / 1000) + "";
        String r = getRandomStr();
        return createDS("DG8lqMyc9gquwAUFc7zBS62ijQRX9XF7", i, r);
    }

    private String getRandomStr() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            String CONSTANTS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int number = random.nextInt(CONSTANTS.length());
            char charAt = CONSTANTS.charAt(number);
            sb.append(charAt);
        }
        return sb.toString();
    }

    private String createDS(String n, String i, String r) {
        String c = DigestUtils.md5Hex("salt=" + n + "&t=" + i + "&r=" + r);
        return String.format("%s,%s,%s", i, r, c);
    }

    /**
     * 封装获取最终cookie请求头
     * @param deviceId 设备iD
     * @param cookie 原cookie
     * @return 请求头map
     */
    private Map<String, String> getFinalCookieMap(String deviceId, String cookie) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json, text/plain, */*");
        headerMap.put("Accept-Language", "zh-CN,zh;q=0.9");
        headerMap.put("Connection", "keep-alive");
        headerMap.put("Cookie", cookie);
        headerMap.put("Origin", "https://user.mihoyo.com");
        headerMap.put("Referer", "https://user.mihoyo.com/");
        headerMap.put("Sec-Fetch-Dest", "empty");
        headerMap.put("Sec-Fetch-Mode", "cors");
        headerMap.put("Sec-Fetch-Site", "same-site");
        headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
        headerMap.put("sec-ch-ua", MiHoYoConfig.DeviceConfig.SEC_CH_UA);
        headerMap.put("sec-ch-ua-mobile", "?0");
        headerMap.put("sec-ch-ua-platform", MiHoYoConfig.DeviceConfig.SEC_CH_UA_PLATFORM);
        headerMap.put("x-rpc-app_id", "undefined");
        headerMap.put("x-rpc-client_type", "4");
        headerMap.put("x-rpc-device_fp", "");
        headerMap.put("x-rpc-device_id", deviceId);
        headerMap.put("x-rpc-device_model", MiHoYoConfig.DeviceConfig.X_RPC_DEVICE_MODEL_PC);
        headerMap.put("x-rpc-device_name", MiHoYoConfig.DeviceConfig.X_RPC_DEVICE_NAME_PC);
        headerMap.put("x-rpc-device_os", "Windows%2010%2064-bit");
        headerMap.put("x-rpc-game_biz", "undefined");
        headerMap.put("x-rpc-lifecycle_id", "undefined");
        headerMap.put("x-rpc-mi_referrer", "https://user.mihoyo.com/#/login/captcha");
        headerMap.put("x-rpc-sdk_version", "");
        return headerMap;
    }

    /**
     * 获取最终cookie 含cookie_token account_id login_ticket login_uid
     * @param headerMap 请求头
     * @return 结果
     */
    private Triple<Boolean, String, String> getFinalCookie(Map<String, String> headerMap) {
        HttpResponse execute = HttpUtil.createGet(MiHoYoConfig.GET_ACCOUNT_INFO_BY_LOGIN_TICKET_URL)
                .addHeaders(headerMap).execute();
        if (execute.getStatus() != 200) {
            String msg = "获取最终token请求失败：["+ execute.getStatus() +"]";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        StringJoiner cookieJoiner = new StringJoiner("; ", "", "");
        cookieJoiner.add(headerMap.get("Cookie"));

        HttpCookie accountId = execute.getCookie("account_id");

        if (accountId != null) {
            cookieJoiner.add(accountId.getName() + "=" + accountId.getValue());
        } else {
            String msg = "获取最终token失败：找不到Cookie的account_id";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }

        HttpCookie cookieToken = execute.getCookie("cookie_token");
        if (cookieToken != null) {
            cookieJoiner.add(cookieToken.getName() + "=" + cookieToken.getValue());
        } else {
            String msg = "获取最终token失败：找不到Cookie的cookie_token";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        return ImmutableTriple.of(true, "成功", cookieJoiner.toString());
    }

    /**
     * 通过cookie登录
     * @param headerMap 请求头带cookie
     * @return 结果
     */
    private Pair<Boolean, String> loginByCookie(Map<String, String> headerMap) {
        Map<String, Object> paramMap = new HashMap<>(1);
        paramMap.put("t", System.currentTimeMillis());
        JSONObject resultJson = HttpUtils.doGet(MiHoYoConfig.LOGIN_BY_COOKIE_URL, mapToHeader(headerMap), paramMap);

        if (resultJson == null) {
            String msg = "cookie登录失败：返回为空";
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }
        if (resultJson.getIntValue("code") != 200) {
            String msg = "cookie登录失败：错误码为：" + resultJson.getIntValue("code");
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }

        JSONObject data = resultJson.getJSONObject("data");
        if (data == null) {
            String msg = "cookie登录失败：data返回为空";
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }

        if (data.getIntValue("status") != 1) {
            String msg = "cookie登录失败：["+ data.getIntValue("status") +"]:" + data.getString("msg");
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }
        return ImmutablePair.of(true, "成功");
    }

    /**
     * 封装发送code的map
     * @param headerMap 请求头
     * @param mmtData mmt参数
     * @param phone 手机号
     * @return 结果
     */
    private Triple<Boolean, String, Map<String, Object>> generateSendCodeMap(Map<String, String> headerMap, JSONObject mmtData, String phone) {
        Map<String, Object> sendCodeMap = new HashMap<>(4);
        String mmtKey = mmtData.getString("mmt_key");
        sendCodeMap.put("mmt_key", mmtKey);
        sendCodeMap.put("action_type", "login");
        sendCodeMap.put("mobile", phone);
        sendCodeMap.put("t", System.currentTimeMillis());
        if (mmtData.containsKey("gt")) {
            // 需要通过人机验证
            String gt = mmtData.getString("gt");
            Triple<Boolean, String, String> codeResult = CaptchaUtil.getValidateV4ByRrOcr(gt, mmtKey, headerMap.get("Referer"));
            if (!codeResult.getLeft()) {
                String msg = "获取人机验证码失败：" + codeResult.getMiddle();
                log.error(msg);
                return ImmutableTriple.of(false, msg, null);
            }

            String v4Data = codeResult.getRight();
            sendCodeMap.put("geetest_v4_data", v4Data);
        }
        return ImmutableTriple.of(true, "成功", sendCodeMap);
    }

    /**
     * 发送手机验证码
     * @param headerMap 请求头
     * @param sendMobileCodeMap 发送param
     * @return 结果
     */
    private Pair<Boolean, String> sendMobileCode(Map<String, String> headerMap, Map<String, Object> sendMobileCodeMap) {
        JSONObject resJson = HttpUtils.doGet(MiHoYoConfig.CREATE_MOBILE_CAPTCHA_URL, mapToHeader(headerMap), sendMobileCodeMap);
        if (resJson == null) {
            String msg = "发送短信失败：返回为空";
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }
        if (resJson.getIntValue("code") != 200) {
            String msg = "发送短信失败：code = " + resJson.getIntValue("code");
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }

        JSONObject data = resJson.getJSONObject("data");
        if (data == null) {
            String msg = "发送短信失败：data返回为空";
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }

        if (data.getIntValue("status") != 1) {
            String msg = "发送短信失败：["+ data.getIntValue("status") +"]:" + data.getString("msg");
            log.error(msg);
            return ImmutablePair.of(false, msg);
        }
        return ImmutablePair.of(true, null);
    }

    /**
     * 生成mmt_key
     * @param headerMap 请求头
     * @return 结果
     */
    private Triple<Boolean, String, JSONObject> getMmtKey(Map<String, String> headerMap) {
        Header[] headers = mapToHeader(headerMap);
        Map<String, Object> paramMap = new HashMap<>(5);
        long now = System.currentTimeMillis();
        paramMap.put("scene_type", 1);
        paramMap.put("now", now);
        paramMap.put("reason", "user.mihoyo.com#/login/captcha");
        paramMap.put("action_type", "login_by_mobile_captcha");
        paramMap.put("t", now);
        JSONObject mmtResult = HttpUtils.doGet(MiHoYoConfig.CREATE_MMT_URL, headers, paramMap);
        if (mmtResult == null) {
            log.error("getCaptcha 请求mmt失败,mmt为空");
            return ImmutableTriple.of(false, "请求mmt失败,请检查网络是否顺畅", null);
        }

        if (mmtResult.getIntValue("code") != 200) {
            log.error("getCaptcha 请求mmt失败:{}", mmtResult.getString("msg"));
            return ImmutableTriple.of(false, "请求mmt失败: " + mmtResult.getString("msg"), null);
        }
        JSONObject data = mmtResult.getJSONObject("data");
        if (data == null) {
            log.error("getCaptcha 请求mmt失败: data为空");
            return ImmutableTriple.of(false, "请求mmt失败: data为空", null);
        }
        JSONObject mmtData = data.getJSONObject("mmt_data");
        if (mmtData == null) {
            log.error("getCaptcha 请求mmt失败: mmt_data为空");
            return ImmutableTriple.of(false, "请求mmt失败: mmt_data为空", null);
        }
        return ImmutableTriple.of(true, "成功", mmtData);
    }

    /**
     * headerMap转header
     * @param headerMap map的header
     * @return header属猪
     */
    private Header[] mapToHeader(Map<String, String> headerMap) {
        MiHoYoAbstractSign.HeaderBuilder.Builder builder = new MiHoYoAbstractSign.HeaderBuilder.Builder();
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            String headerName = entry.getKey();
            String headerValue = entry.getValue();
            builder.add(headerName, headerValue);
        }
        return builder.build();
    }

    /**
     * 获取通用请求头
     * @return 通用头
     */
    private Map<String, String> getCaptchaHeaderMap() {
        Map<String, String> map = new HashMap<>();
        map.put("Host", "webapi.account.mihoyo.com");
        map.put("Connection", "keep-alive");
        map.put("sec-ch-ua", MiHoYoConfig.DeviceConfig.SEC_CH_UA);
        map.put("DNT", "1");
        map.put("x-rpc-device_model", MiHoYoConfig.DeviceConfig.X_RPC_DEVICE_MODEL_PC);
        map.put("sec-ch-ua-mobile", "?0");
        map.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
//        map.put("x-rpc-device_id", None);
        map.put("Accept", "application/json, text/plain, */*");
        map.put("x-rpc-device_name", MiHoYoConfig.DeviceConfig.X_RPC_DEVICE_NAME_PC);
        map.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        map.put("x-rpc-client_type", "4");
        map.put("sec-ch-ua-platform", MiHoYoConfig.DeviceConfig.SEC_CH_UA_PLATFORM);
        map.put("Origin", "https://user.mihoyo.com");
        map.put("Sec-Fetch-Site", "same-site");
        map.put("Sec-Fetch-Mode", "cors");
        map.put("Sec-Fetch-Dest", "empty");
        map.put("Referer", "https://user.mihoyo.com/");
        map.put("Accept-Encoding", "gzip, deflate, br");
        map.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        return map;
    }

    /**
     * 登录获取cookie以及token
     * @param headerMap 请求头
     * @param phone 手机号
     * @param captcha 验证码
     * @return 结果
     */
    private Triple<Boolean, String, String> loginAndGetCookie(Map<String, String> headerMap, String phone, String captcha) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mobile", phone);
        paramMap.put("mobile_captcha", captcha);
        paramMap.put("source", "user.mihoyo.com");
        paramMap.put("t", System.currentTimeMillis());
        HttpResponse execute = HttpUtil.createPost(MiHoYoConfig.LOGIN_BY_MOBILE_CAPTCHA_URL)
                .addHeaders(headerMap)
                .form(paramMap).execute();
        if (execute.getStatus() != 200) {
            String msg = "验证码登录请求失败：["+ execute.getStatus() +"]";
            log.warn(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        JSONObject resultJson = JSON.parseObject(execute.body());
        if (resultJson == null) {
            String msg = "验证码登录失败：返回为空";
            log.error(msg);
            return ImmutableTriple.of(false, msg, null);
        }
        if (resultJson.getIntValue("code") != 200) {
            String msg = "验证码登录失败：错误码为：" + resultJson.getIntValue("code");
            log.error(msg);
            return ImmutableTriple.of(false, msg, null);
        }

        JSONObject data = resultJson.getJSONObject("data");
        if (data == null) {
            String msg = "验证码登录失败：data返回为空";
            log.error(msg);
            return ImmutableTriple.of(false, msg, null);
        }

        if (data.getIntValue("status") != 1) {
            String msg = "验证码登录失败：["+ data.getIntValue("status") +"]:" + data.getString("msg");
            log.error(msg);
            return ImmutableTriple.of(false, msg, null);
        }

        StringJoiner cookieJoiner = new StringJoiner("; ", "", "");
        HttpCookie aliyungfTc = execute.getCookie("aliyungf_tc");
        if (aliyungfTc != null) {
            cookieJoiner.add(aliyungfTc.getName() + "=" + aliyungfTc.getValue());
        }

        HttpCookie loginUid = execute.getCookie("login_uid");
        if (loginUid != null) {
            cookieJoiner.add(loginUid.getName() + "=" + loginUid.getValue());
        }

        HttpCookie loginTicket = execute.getCookie("login_ticket");
        if (loginTicket != null) {
            cookieJoiner.add(loginTicket.getName() + "=" + loginTicket.getValue());
        }
        headerMap.put("Cookie", cookieJoiner.toString());
        return ImmutableTriple.of(true, "成功", cookieJoiner.toString());
    }
}
