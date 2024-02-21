package com.miyoushe.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miyoushe.mapper.AutoMihayouDao;
import com.miyoushe.model.AutoMihayou;
import com.miyoushe.sign.constant.MihayouConstants;
import com.miyoushe.sign.gs.GenShinSignMiHoYo;
import com.miyoushe.sign.gs.MiHoYoConfig;
import com.miyoushe.sign.gs.MiHoYoSignMiHoYo;
import com.miyoushe.sign.gs.StarRailSignMiHoYo;
import com.oldwu.constant.URLConstant;
import com.oldwu.dao.AutoLogDao;
import com.oldwu.dao.UserDao;
import com.oldwu.entity.AjaxResult;
import com.oldwu.entity.AutoLog;
import com.oldwu.security.utils.SessionUtils;
import com.oldwu.task.MiHuYouTask;
import com.oldwu.util.HttpUtils;
import com.oldwu.vo.PageDataVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class MihayouService {
    private final Log logger = LogFactory.getLog(MihayouService.class);


    @Autowired
    private AutoMihayouDao mihayouDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AutoLogDao autoLogDao;

    /**
     * 根据id查询
     *
     * @return
     */
    public AjaxResult view(Integer id) {
        Integer userId = SessionUtils.getPrincipal().getId();
        AutoMihayou autoMihayou = mihayouDao.selectById(id);
        if (autoMihayou == null) {
            return AjaxResult.doError();
        } else {
            //放行管理员
            String role = userDao.getRole(userId);
            if (!autoMihayou.getUserId().equals(userId) && !role.equals("ROLE_ADMIN")) {
                return AjaxResult.doError("你无权访问！");
            }
        }
        //移除cookie
        autoMihayou.setCookie(null);
        autoMihayou.setStoken(null);
        autoMihayou.setOtherKey(null);
        autoMihayou.setLcookie(null);
        return AjaxResult.doSuccess(autoMihayou);
    }


    public PageDataVO<AutoMihayou> queryPageList(Integer page, Integer limit) {

        QueryWrapper<AutoMihayou> queryWrapper = new QueryWrapper<>();
        Page<AutoMihayou> pageObj = new Page<>(page, limit);
        IPage<AutoMihayou> data = mihayouDao.selectPage(pageObj, queryWrapper);

        List<AutoMihayou> autoMihayouList = data.getRecords();

        for (AutoMihayou mihayous : autoMihayouList) {
            mihayous.setCookie(null);
            mihayous.setStoken(null);
            mihayous.setSuid(null);
            mihayous.setLcookie(null);
            mihayous.setWebhook(null);
            mihayous.setOtherKey(null);
            mihayous.setGenshinUid(com.oldwu.util.StringUtils.userNameEncode(mihayous.getGenshinUid()));
            mihayous.setMiName(com.oldwu.util.StringUtils.userNameEncode(mihayous.getMiName()));
        }

        data.setRecords(autoMihayouList);

        return new PageDataVO<>(data.getTotal(), data.getRecords());
    }

    /**
     * 删除米游社任务，开启事务
     *
     * @param id 传入要删除的autoId
     * @return AjaxResult 删除结果
     */
    @Transactional
    public AjaxResult deleteMiHuYouPlan(Integer id) throws Exception {
        //校验用户id
        Integer userid = SessionUtils.getPrincipal().getId();
        if (id == null || id == 0) {
            return AjaxResult.doError("传参不能为空！");
        }
        List<AutoMihayou> autoMihayous = mihayouDao.selectMine(userid);
        boolean flag = false;
        for (AutoMihayou mihayou : autoMihayous) {
            int autoId = mihayou.getId();
            if (autoId == id) {
                flag = true;
                break;
            }
        }
        if (userDao.getRole(userid).equals("ROLE_ADMIN")) {  //忽略管理
            flag = true;
        }
        if (!flag) {
            return AjaxResult.doError("你没有权限删除这条或数据不存在！");
        }
        //首先删除日志
        AutoLog autoLog = new AutoLog();
        autoLog.setUserid(userid);
        autoLog.setAutoId(id);
        autoLog.setType("mihuyou");
        autoLogDao.deleteByAutoId(autoLog);
        //最后删除主要数据
        int i = mihayouDao.deleteById(id);
        if (i > 0) {
            return AjaxResult.doSuccess("删除成功");
        }
        //删除失败后回滚
        throw new Exception("删除失败！");
    }

    public List<Map<String, String>> addMiHuYouPlan(AutoMihayou autoMihayou) {
        List<Map<String, String>> list = new ArrayList<>();

        Map<String, Object> stringObjectMap = checkForm(autoMihayou, false);
        if (!(boolean) stringObjectMap.get("flag")) {
            Map<String, String> map = new HashMap<>();
            map.put("code", "-1");
            map.put("msg", (String) stringObjectMap.get("msg"));
            list.add(map);
            return list;
        }

        boolean isPass = setAutoMiHayou(autoMihayou, stringObjectMap, list);


        if (!isPass) {
            return list;
        }

        //判断数据是否存在，使用stuid进行检索
        AutoMihayou autoMihayou1 = mihayouDao.selectBystuid(autoMihayou.getSuid());
        if (autoMihayou1 == null || autoMihayou1.getId() == null) {
            //insert
            mihayouDao.insert(autoMihayou);
        } else {
            //update
            autoMihayou.setId(autoMihayou1.getId());
            mihayouDao.updateById(autoMihayou);
        }
        return list;
    }

    private boolean setAutoMiHayou(AutoMihayou autoMihayou, Map<String, Object> stringObjectMap, List<Map<String, String>> list) {
        autoMihayou.setSuid((String) stringObjectMap.get("stuid"));
        autoMihayou.setStoken((String) stringObjectMap.get("stoken"));
        autoMihayou.setOtherKey((String) stringObjectMap.get("login_ticket_str"));
        try {
            Map<String, Object> personalInfo = getPersonalInfo(autoMihayou.getCookie(), autoMihayou.getSuid(), autoMihayou.getStoken());
            autoMihayou.setAvatar((String) personalInfo.get("avatar_url"));
            autoMihayou.setMiName((String) personalInfo.get("nickname"));
        } catch (Exception e) {
            logger.warn("获取头像失败！" + autoMihayou);
        }
        autoMihayou.setStatus("100");

        // 信息检查完毕后，尝试登录账号，进行验证
        GenShinSignMiHoYo signMiHoYo = new GenShinSignMiHoYo(autoMihayou.getCookie());
        List<Map<String, Object>> genshinUidInfo = signMiHoYo.getUid();

        boolean isPassGenshin = checkUidInfo(genshinUidInfo, list, stringObjectMap);
        if (!isPassGenshin) {
            logger.warn("原神获取失败");
        } else {
            String uid = "";
            String nickname = "";
            for (int i = 0; i < genshinUidInfo.size(); i++) {
                Map<String, Object> map1 = genshinUidInfo.get(i);
                if (i == 0) {
                    uid = (String) map1.get("uid");
                    nickname = (String) map1.get("nickname");
                } else {
                    uid = uid + "," + map1.get("uid");
                    nickname = nickname + "," + map1.get("nickname");
                }
            }
            autoMihayou.setGenshinUid(uid);
            autoMihayou.setGenshinName(nickname);
        }

        // 获取崩铁信息
        StarRailSignMiHoYo starRailSignMiHoYo = new StarRailSignMiHoYo(autoMihayou.getCookie());
        List<Map<String, Object>> starRailSignMiHoYoUid = starRailSignMiHoYo.getUid();

        boolean isPassStarRail = checkUidInfo(starRailSignMiHoYoUid, list, stringObjectMap);
        if (!isPassStarRail) {
            logger.warn("崩铁获取失败");
        } else {
            String starRailUid = "";
            String starRailNickname = "";
            for (int i = 0; i < starRailSignMiHoYoUid.size(); i++) {
                Map<String, Object> map1 = starRailSignMiHoYoUid.get(i);
                if (i == 0) {
                    starRailUid = (String) map1.get("uid");
                    starRailNickname = (String) map1.get("nickname");
                } else {
                    starRailUid = starRailUid + "," + map1.get("uid");
                    starRailNickname = starRailNickname + "," + map1.get("nickname");
                }
            }
            autoMihayou.setStarRailUid(starRailUid);
            autoMihayou.setStarRailName(starRailNickname);
        }

        return isPassGenshin || isPassStarRail;
    }

    private boolean checkUidInfo(List<Map<String, Object>> genshinUidInfo, List<Map<String, String>> list, Map<String, Object> stringObjectMap) {
        //账号都是同一个，如果要错一起错，一般不会出现不一致的情况
        if (!(boolean) genshinUidInfo.get(0).get("flag")) {
            Map<String, String> map = new HashMap<>();
            map.put("code", "-1");
            map.put("msg", (String) genshinUidInfo.get(0).get("msg"));
            list.add(map);
            return false;
        }
        //账号验证成功,写入用户数据，如果有多个数据则拿逗号分隔

        for (Map<String, Object> uidInfoMap : genshinUidInfo) {
            Map<String, String> map1 = new HashMap<>();
            map1.put("code", "200");
            map1.put("msg", (String) uidInfoMap.get("msg"));
            if (!stringObjectMap.containsKey("login_ticket_str") || stringObjectMap.containsKey("login_ticket") && !(Boolean) stringObjectMap.get("login_ticket")) {
                map1.put("code", "201");
                map1.put("msg", map1.get("msg") + "<br>");
                map1.put("msg1", (String) stringObjectMap.get("msg"));
            }
            list.add(map1);
        }
        return true;
    }

    public Map<String, Object> checkForm(AutoMihayou autoMihayou, boolean skipCookieCheck) {
        Map<String, Object> map = new HashMap<>();
        String lcookie = autoMihayou.getLcookie();
        String enable = autoMihayou.getEnable();
        String webhook = autoMihayou.getWebhook();
        if (!skipCookieCheck) {
            String name = autoMihayou.getName();
            String cookie = autoMihayou.getCookie();
            if (StringUtils.isBlank(name) || StringUtils.isBlank(cookie)) {
                map.put("flag", false);
                map.put("msg", "前两项不能为空！");
                return map;
            }
            //检查cookie字段
            String account_id = HttpUtils.getCookieByName(autoMihayou.getCookie(), "account_id");
            String cookie_token = HttpUtils.getCookieByName(autoMihayou.getCookie(), "cookie_token");
            if (StringUtils.isBlank(account_id)) {
                //备用方案
                account_id = HttpUtils.getCookieByName(autoMihayou.getCookie(), "ltuid");
            }
            if (!StringUtils.isBlank(lcookie)) {
                //校验两个cookie字段
                String login_uid = HttpUtils.getCookieByName(lcookie, "login_uid");
                if (!StringUtils.isBlank(login_uid) && !StringUtils.isBlank(account_id)) {
                    if (!login_uid.equals(account_id)) {
                        map.put("flag", false);
                        map.put("msg", "两项cookie中的账号信息不一致！");
                        return map;
                    }
                }
                if (StringUtils.isBlank(account_id)) {
                    //备用方案2
                    account_id = HttpUtils.getCookieByName(lcookie, "login_uid");
                }
                String login_ticket = HttpUtils.getCookieByName(lcookie, "login_ticket");
                if (StringUtils.isBlank(account_id) || StringUtils.isBlank(login_ticket)) {
                    map.put("login_ticket", false);
                    map.put("msg", "cookie中没有login_ticket字段或login_ticket已过期，无法使用米游币任务，如果需要使用，请前往米哈游通行证处获取");
                } else {
                    //获取stoken
                    Map<String, Object> cookieToken = getCookieToken(login_ticket, account_id);
                    if (!(boolean) cookieToken.get("flag")) {
                        map.put("login_ticket", false);
                        map.put("msg", "login_ticket校验失败：" + cookieToken.get("msg") + "你可能无法使用米游币任务！");
                    } else {
                        map.put("login_ticket_str", login_ticket);
                        map.put("stoken", cookieToken.get("stoken"));
                    }
                }
            } else {
                map.put("msg", "没有填写米哈游通行证cookie，如果您无需使用米游币任务，请忽略此消息");
            }
            map.put("stuid", account_id);
            if (StringUtils.isBlank(account_id) || StringUtils.isBlank(cookie_token)) {
                map.put("flag", false);
                map.put("msg", "无效的cookie：米游社cookie中必须包含cookie_token和account_id字段！");
                return map;
            }
        } else {
            if (!StringUtils.isBlank(lcookie) && autoMihayou.getId() != null) {
                String login_uid = HttpUtils.getCookieByName(lcookie, "login_uid");
                String suid = mihayouDao.selectById(autoMihayou.getId()).getSuid();
                if (!StringUtils.isBlank(login_uid) && !StringUtils.isBlank(suid)) {
                    if (!login_uid.equals(suid)) {
                        map.put("flag", false);
                        map.put("msg", "与之前添加的账号信息不一致！");
                        return map;
                    }
                }
                String login_ticket = HttpUtils.getCookieByName(lcookie, "login_ticket");
                if (!StringUtils.isBlank(suid) && !StringUtils.isBlank(login_ticket)) {
                    //获取stoken
                    Map<String, Object> cookieToken = getCookieToken(login_ticket, suid);
                    if ((boolean) cookieToken.get("flag")) {
                        autoMihayou.setOtherKey(login_ticket);
                        autoMihayou.setStoken((String) cookieToken.get("stoken"));
                    } else {
                        map.put("flag", false);
                        map.put("msg", "login_ticket校验失败：" + cookieToken.get("msg"));
                    }
                } else {
                    map.put("flag", false);
                    map.put("msg", "没有发现login_ticket字段！");
                }
            }
        }
        if (StringUtils.isBlank(enable) || !enable.equals("true") && !enable.equals("false")) {
            autoMihayou.setEnable("true");
        }
        if (StringUtils.isBlank(webhook)) {
            autoMihayou.setWebhook("");
        }
        //返回ltuid和token
        if (map.containsKey("flag") && !(boolean) map.get("flag")) {
            return map;
        }
        map.put("flag", true);
        return map;
    }

    public Map<String, Object> getCookieToken(String login_ticket, String accountId) {
        Map<String, Object> map = new HashMap<>();
        String token_url = String.format(MiHoYoConfig.MYS_TOKEN_URL, login_ticket, accountId);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("x-rpc-app_version", "2.11.2");
        headerMap.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) miHoYoBBS/2.11.1");
        headerMap.put("x-rpc-client_type", "5");
        headerMap.put("Referer", "https://webstatic.mihoyo.com/");
        headerMap.put("Origin", "https://webstatic.mihoyo.com");
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtils.doGet(token_url, null, headerMap, null);
            JSONObject result = HttpUtils.getJson(httpResponse);
            if (!"OK".equals(result.get("message"))) {
                map.put("flag", false);
                map.put("msg", "login_ticket已失效,请重新登录获取");
//                System.err.println("login_ticket已失效,请重新登录获取");
                return map;
            }
            map.put("flag", true);
            map.put("msg", "OK");
            map.put("stoken", result.getJSONObject("data").getJSONArray("list").getJSONObject(0).getString("token"));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("flag", false);
            map.put("msg", "服务端请求失败！" + e.getMessage());
            return map;
        }
    }

    public Map<String, Object> editMiHuYouPlan(AutoMihayou autoMihayo1) {
        Map<String, Object> map = new HashMap<>();
        AutoMihayou autoMihayou = mihayouDao.selectById(autoMihayo1.getId());
        if (autoMihayou == null || autoMihayou.getId() == null) {
            map.put("code", -1);
            map.put("msg", "参数错误！");
            return map;
        }
        //放行管理员
        String role = userDao.getRole(autoMihayo1.getUserId());
        if (!autoMihayou.getUserId().equals(autoMihayo1.getUserId()) && !role.equals("ROLE_ADMIN")) {
            map.put("code", 403);
            map.put("msg", "你没有权限修改！");
            return map;
        }
//        Map<String, Object> formResult = checkForm(autoMihayo1, true);
        if (StrUtil.isBlank(autoMihayo1.getCookie())) {
            autoMihayo1.setCookie(autoMihayou.getCookie());
        }
        Map<String, Object> formResult = checkForm(autoMihayo1, false);
        if (!(boolean) formResult.get("flag")) {
            map.put("code", 201);
            map.put("msg", formResult.get("msg"));
            return map;
        }

        setAutoMiHayou(autoMihayo1, formResult, new ArrayList<>());

        int i = mihayouDao.updateById(autoMihayo1);
        if (i > 0) {
            map.put("code", 200);
            map.put("msg", "操作成功！");
            return map;
        }
        map.put("code", 0);
        map.put("msg", "操作失败！");
        return map;
    }

    public AjaxResult doDailyTaskPersonal(Integer autoId) {
        Integer userId = SessionUtils.getPrincipal().getId();

        AutoMihayou autoMihayou = mihayouDao.selectById(autoId);
        if (autoMihayou == null || autoMihayou.getId() == null) {
            return AjaxResult.doError("参数错误！");
        }
        String role = userDao.getRole(autoMihayou.getUserId());
        if (!autoMihayou.getUserId().equals(userId) && !role.equals("ROLE_ADMIN")) {
            return AjaxResult.doError("你没有权限执行！");
        }
        if (autoMihayou.getStatus().equals("1")) {
            return AjaxResult.doError("任务已经在运行啦~请不要重复执行");
        }
        Thread t = new Thread(() -> {
            MiHuYouTask miHuYouTask = new MiHuYouTask();
            miHuYouTask.runTask(autoId, userId, autoMihayou);
        });
        t.start();
        return AjaxResult.doSuccess("运行指令已发送，请稍后查看运行状态");
    }

    public AjaxResult listMine(Integer id) {
        List<AutoMihayou> autoMihayous = mihayouDao.selectMine(id);
        return AjaxResult.doSuccess(autoMihayous);
    }

    /**
     * 获取米游社账号各种信息，目前仅用于头像获取
     *
     * @param cookie
     * @param suid
     * @param stoken
     */
    public Map<String, Object> getPersonalInfo(String cookie, String suid, String stoken) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> headers = HttpUtils.getHeaders();
        headers.put("Cookie", cookie);
        headers.put("Referer", "https://www.miyoushe.com");
        headers.put("x_rpc_app_version", MihayouConstants.APP_VERSION);
        headers.put("x_rpc_device_id", UUID.randomUUID().toString().replace("-", "").toUpperCase());
        headers.put("x_rpc_client_type", MihayouConstants.SIGN_CLIENT_TYPE);
        headers.put("DS", new MiHoYoSignMiHoYo(MiHoYoConfig.HubsEnum.YS.getGame(), suid, stoken).getDS());
        HttpResponse httpResponse = HttpUtils.doGet(MiHoYoConfig.MYS_PERSONAL_INFO_URL, "", headers, null);
        JSONObject json = HttpUtils.getJson(httpResponse);
        if (json.getInteger("retcode") != 0) {
            return null;
        }
        JSONObject data = json.getJSONObject("data");
        JSONObject userInfo = data.getJSONObject("user_info");
        map.put("avatar_url", userInfo.getString("avatar_url"));
        map.put("nickname", userInfo.getString("nickname"));
        return map;
    }

    @Async
    public void setPersonInfo(Integer id, String cookie) {
        try {
            Map<String, Object> personalInfo = getPersonalInfo(cookie, null, null);
            if (personalInfo == null) {
                return;
            }
            String avatarUrl = (String) personalInfo.get("avatar_url");
            AutoMihayou autoMihayou = new AutoMihayou();
            autoMihayou.setId(id);
            autoMihayou.setAvatar(avatarUrl);
            mihayouDao.updateById(autoMihayou);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
