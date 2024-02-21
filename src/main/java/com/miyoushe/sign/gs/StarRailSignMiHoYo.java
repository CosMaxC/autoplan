package com.miyoushe.sign.gs;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.captcha.util.CaptchaUtil;
import com.miyoushe.sign.constant.MihayouConstants;
import com.miyoushe.sign.gs.pojo.Award;
import com.miyoushe.util.HttpUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author :
 * @date :2023/12/13 18:50
 * @description :
 */
public class StarRailSignMiHoYo extends MiHoYoAbstractSign  {

    private static final Logger log = LogManager.getLogger(HttpUtils.class.getName());

    private String uid;

    public void setUid(String uid) {
        this.uid = uid;
    }

    public StarRailSignMiHoYo(String cookie) {
        super(cookie);
        setClientType(MihayouConstants.SIGN_CLIENT_TYPE);
        setAppVersion(MihayouConstants.STAR_RAIL_APP_VERSION);
        setSalt(MihayouConstants.STAR_RAIL_SIGN_SALT);
    }

    @Override
    public String getDS() {
        String i = (System.currentTimeMillis() / 1000) + "";
        String r = getRandomStr();
        return createDS(MihayouConstants.STAR_RAIL_SIGN_SALT, i, r);
    }

    private String createDS(String n, String i, String r) {
        String c = DigestUtils.md5Hex("salt=" + n + "&t=" + i + "&r=" + r);
        return String.format("%s,%s,%s", i, r, c);
    }

    @Override
    public Header[] getHeaders(String dsType) {
//        String ds;
//        if (StrUtil.isNotBlank(dsType)) {
//            ds = getDS(dsType);
//        } else {
//            ds = getDS();
//        }
        return new HeaderBuilder.Builder().addAll(getBasicHeaders())
                .add("x-rpc-device_id", UUID.randomUUID().toString().replace("-", "").toUpperCase())
                .add("Content-Type", "application/json;charset=UTF-8")
                .add("x-rpc-client_type", getClientType())
                .add("x-rpc-app_version", getAppVersion())
//                .add("x-rpc-signgame", "hk4e")
                .add("Origin", MiHoYoConfig.NEW_SIGN_ORIGIN)
                .add("Referer", MiHoYoConfig.NEW_SIGN_ORIGIN)
                .add("DS", getDS()).build();
    }

    @Override
    public Header[] getBasicHeaders() {
        return new HeaderBuilder.Builder()
                .add("Accept", "application/json, text/plain, */*")
                .add("x-rpc-channel", "miyousheluodi")
                .add("User-Agent", String.format(MiHoYoConfig.USER_AGENT_TEMPLATE, getAppVersion()))
//                .add("x-rpc-channel", "appstore")
                .add("Cookie", cookie)
                .add("Referer", MiHoYoConfig.NEW_SIGN_ORIGIN)
                .add("Accept-Encoding", "gzip, deflate")
                .add("Accept-Language", "zh-CN,en-US;q=0.8")
                .add("accept-encoding", "gzip, deflate")
                .add("accept-encoding", "gzip, deflate")
                .add("X-Requested-With", "com.mihoyo.hyperion")
                .add("Host", "api-takumi.mihoyo.com").build();
    }

    /**
     * 获取uid
     *
     * @return
     */
    public List<Map<String, Object>> getUid() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        try {

            JSONObject result = HttpUtils.doGet(MiHoYoConfig.STAR_RAIL_ROLE_URL, getBasicHeaders());
            if (result == null) {
                map.put("flag", false);
                map.put("msg", "获取uid失败，cookie可能有误！");
                list.add(map);
                return list;
            }

            JSONArray jsonArray = result.getJSONObject("data").getJSONArray("list");

            for (Object user : jsonArray) {
                JSONObject userInfo = (JSONObject) user;
                String uid = userInfo.getString("game_uid");
                String nickname = userInfo.getString("nickname");
                String regionName = userInfo.getString("region_name");
                String region = userInfo.getString("region");
                String gameBiz = userInfo.getString("game_biz");

                log.info("获取用户UID：{}", uid);
                log.info("当前用户名称：{}", nickname);
                log.info("当前用户服务器：{}", regionName);

                setUid(uid);

                Map<String, Object> mapInfo = new HashMap<>();
                mapInfo.put("uid", uid);
                mapInfo.put("nickname", nickname);
                mapInfo.put("game_biz", gameBiz);
                mapInfo.put("region", region);
                mapInfo.put("flag", true);
                mapInfo.put("msg", "登录成功！用户UID：" + uid + "，用户名：" + nickname);

                list.add(mapInfo);
            }

            return list;
        } catch (Exception e) {
            map.put("flag", false);
            map.put("msg", "获取uid失败，未知异常：" + e.getMessage());

            list.add(map);
            return list;
        }
    }

    @Override
    public List<Map<String, Object>> doSign() {
        List<Map<String, Object>> uid = getUid();

        for (Map<String, Object> uidMap : uid) {
            if (!(boolean) uidMap.get("flag")) {
                continue;
            }

            String doSign = doSign((String) uidMap.get("uid"), (String) uidMap.get("region"));
            String hubSign = hubSign((String) uidMap.get("uid"), (String) uidMap.get("region"));
            uidMap.put("msg", uidMap.get("msg") + "\n" + doSign + "\n" + hubSign);
        }
        return uid;
    }
    /**
     * 签到（重载doSign,主要用来本地测试）
     * @param uid 游戏角色uid
     * @param region 游戏服务器标识符
     * @return 签到信息
     */
    public String doSign(String uid, String region) {

        Map<String, Object> data = new HashMap<>();

        data.put("act_id", MiHoYoConfig.STAR_RAIL_ACT_ID);
        data.put("region", region);
        data.put("uid", uid);

        JSONObject signResult = HttpUtils.doPost(MiHoYoConfig.SIGN_URL, getHeaders(null), data);

        if (signResult.getInteger("retcode") == 0) {
            JSONObject dataJson = signResult.getJSONObject("data");
            String gt = dataJson.getString("gt");
            String challenge = dataJson.getString("challenge");
            Boolean isRisk = dataJson.getBoolean("is_risk");
            if (isRisk) {
                Triple<Boolean, String, String> validateByRrOcr = CaptchaUtil.getValidateByRrOcr(gt, challenge, MiHoYoConfig.SIGN_URL);
                if (!validateByRrOcr.getLeft()) {
                    log.error("崩铁：星穹铁道签到福利失败：{}", validateByRrOcr.getMiddle());
                    return "崩铁：星穹铁道签到福利失败：" + validateByRrOcr.getMiddle();
                } else {
                    String validate = validateByRrOcr.getRight();
                    signResult = HttpUtils.doPost(MiHoYoConfig.SIGN_URL, getAccessCaptchaHeaders("", challenge, validate), data);
                    log.info("验证码后崩铁：星穹铁道请求返回：{}", signResult);
                    if (signResult.getInteger("retcode") == 0 && !signResult.getJSONObject("data").getBoolean("is_risk")) {
                        log.info("崩铁：星穹铁道签到福利成功：{}", signResult.get("message"));
                        return "崩铁：星穹铁道签到福利成功：" + signResult.get("message");
                    } else {
                        log.error("崩铁：星穹铁道签到福利签到失败：崩铁：星穹铁道验证码通过但签到失败通过: {}", signResult.get("message"));
                        return "崩铁：星穹铁道签到福利签到失败：崩铁：星穹铁道验证码通过但签到失败通过：" + signResult.get("message");
                    }
                }
            }
            log.info("崩铁：星穹铁道签到福利成功：{}", signResult.get("message"));
            return "崩铁：星穹铁道签到福利成功：" + signResult.get("message");
        } else {
            log.info("崩铁：星穹铁道签到福利签到失败：{}", signResult.get("message"));
            return "崩铁：星穹铁道签到福利签到失败：" + signResult.get("message");
        }
    }

    public Header[] getAccessCaptchaHeaders(String dsType, String challenge, String validate) {
        return new HeaderBuilder.Builder().addAll(getBasicHeaders())
                .add("x-rpc-device_id", UUID.randomUUID().toString().replace("-", "").toUpperCase())
                .add("Content-Type", "application/json;charset=UTF-8")
                .add("x-rpc-client_type", getClientType())
                .add("x-rpc-app_version", getAppVersion())
//                .add("x-rpc-signgame", "hk4e")
                .add("Origin", MiHoYoConfig.NEW_SIGN_ORIGIN)
                .add("Referer", MiHoYoConfig.NEW_SIGN_ORIGIN)
                .add("x-rpc-challenge", challenge)
                .add("x-rpc-validate", validate)
                .add("x-rpc-seccode", validate + "|jordan")
                .add("DS", getDS()).build();
    }


    /**
     * 社区签到并查询当天奖励
     * @param uid 游戏角色uid
     * @param region 游戏服务器标识符
     * @return 签到信息
     */
    public String hubSign(String uid, String region) {
        Map<String, Object> data = new HashMap<>();

        data.put("act_id", MiHoYoConfig.STAR_RAIL_ACT_ID);
        data.put("region", region);
        data.put("uid", uid);

        JSONObject signInfoResult = HttpUtils.doGet(MiHoYoConfig.INFO_URL, getHeaders(""), data);
        if (signInfoResult == null || signInfoResult.getJSONObject("data") == null){
            return null;
        }

        LocalDateTime time = LocalDateTime.now();
        Boolean isSign = signInfoResult.getJSONObject("data").getBoolean("is_sign");
        Integer totalSignDay = signInfoResult.getJSONObject("data").getInteger("total_sign_day");
        int day = isSign ? totalSignDay : totalSignDay + 1;

        Award award = getAwardInfo(day);

        StringBuilder msg = new StringBuilder();
        msg.append(time.getMonth().getValue()).append("月已签到").append(totalSignDay).append("\n");
        msg.append(signInfoResult.getJSONObject("data").get("today")).append("签到获取").append(award.getCnt()).append(award.getName());

        log.info("{}月已签到{}天", time.getMonth().getValue(), totalSignDay);
        log.info("{}签到获取{}{}", signInfoResult.getJSONObject("data").get("today"), award.getCnt(), award.getName());

        return msg.toString();
    }

    /**
     * 获取今天奖励详情
     *
     * @param day
     * @return
     */
    public Award getAwardInfo(int day) {

        JSONObject awardResult = HttpUtils.doGet(MiHoYoConfig.STAR_RAIL_AWARD_URL, getHeaders(""));
        JSONArray jsonArray = awardResult.getJSONObject("data").getJSONArray("awards");

        List<Award> awards = JSON.parseObject(JSON.toJSONString(jsonArray), new TypeReference<List<Award>>() {});
        return awards.get(day - 1);
    }
}
