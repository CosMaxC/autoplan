package com.miyoushe.sign.gs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.captcha.util.CaptchaUtil;
import com.miyoushe.sign.constant.MihayouConstants;
import com.miyoushe.sign.gs.pojo.PostResult;
import com.miyoushe.util.HttpUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @Author ponking
 * @Date 2021/5/26 9:18
 */
public class MiHoYoSignMiHoYo extends MiHoYoAbstractSign {

    /**
     * 浏览帖子数
     */
    private final static int VIEW_NUM = 5;
    /**
     * 点赞帖子数
     */
    private final static int UP_VOTE_NUM = 5;
    /**
     * 分享帖子数
     */
    private final static int SHARE_NUM = 3;

    private static final Logger log = LogManager.getLogger(MiHoYoSignMiHoYo.class.getName());
    private final MiHoYoConfig.Hub hub;
    private final String stuid;
    private final String stoken;
    private final Random random = new Random();
    private final CountDownLatch countDownLatch = new CountDownLatch(3);

    /**
     * 辣鸡服务器1v2学生机，常驻线程不会炸？
     */
    private final ExecutorService pool;

    public MiHoYoSignMiHoYo(MiHoYoConfig.Hub hub, String stuid, String stoken) {
        this(null, hub, stuid, stoken, null);
    }

    public MiHoYoSignMiHoYo(MiHoYoConfig.Hub hub, String stuid, String stoken, ThreadPoolExecutor executor) {
        this(null, hub, stuid, stoken, null);
    }

    public MiHoYoSignMiHoYo(String cookie, MiHoYoConfig.Hub hub, String stuid, String stoken, ThreadPoolExecutor executor) {
        super(cookie);
        this.hub = hub;
        this.stuid = stuid;
        this.stoken = stoken;
        this.pool = executor;
        setClientType(MihayouConstants.COMMUNITY_CLIENT_TYPE);
        setAppVersion(MihayouConstants.STAR_RAIL_APP_VERSION);
        setSalt(MihayouConstants.COMMUNITY_SALT);
    }

    @Override
    public List<Map<String, Object>> doSign() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>(2);
        StringBuilder msg = new StringBuilder();
        log.info("社区签到任务开始");
        msg.append("社区签到任务开始\n");
        String sign = sign();
        msg.append(sign).append("\n");
        List<PostResult> genShinHomePosts = getGenShinHomePosts();
        List<PostResult> homePosts = getPosts();
        genShinHomePosts.addAll(homePosts);
        log.info("获取社区帖子数: {}", genShinHomePosts.size());
        msg.append("获取社区帖子数: ").append(genShinHomePosts.size()).append("\n");
        //执行任务
        int viewPost = doTask(this, this.getClass().getDeclaredMethod("viewPost", PostResult.class), VIEW_NUM, genShinHomePosts);
        int sharePost = doTask(this, this.getClass().getDeclaredMethod("sharePost", PostResult.class), SHARE_NUM, genShinHomePosts);
        int upVotePost = doTask(this, this.getClass().getDeclaredMethod("upVotePost", PostResult.class), UP_VOTE_NUM, genShinHomePosts);
        //打印日志
        log.info("浏览帖子,成功: {},失败：{}", viewPost, VIEW_NUM - viewPost);
        log.info("点赞帖子,成功: {},失败：{}", upVotePost, UP_VOTE_NUM - upVotePost);
        log.info("分享帖子,成功: {},失败：{}", sharePost, SHARE_NUM - sharePost);
        msg.append("浏览帖子,成功: ").append(viewPost).append(",失败：").append(VIEW_NUM - viewPost).append("\n");
        msg.append("点赞帖子,成功: ").append(upVotePost).append(",失败：").append(UP_VOTE_NUM - upVotePost).append("\n");
        msg.append("分享帖子,成功: ").append(sharePost).append(",失败：").append(SHARE_NUM - sharePost).append("\n");
//        pool.shutdown();  会导致阻塞
        log.info("社区签到任务完成");
        msg.append("社区签到任务完成");
        map.put("flag", true);
        map.put("msg", msg.toString());

        list.add(map);
        return list;
    }

    public Map<String, Object> doSingleThreadSign() throws Exception {
        Map<String, Object> map = new HashMap<>(2);
        String msg = "";
        String sign = sign();
        msg = msg + sign + "\n";
        List<PostResult> genShinHomePosts = getGenShinHomePosts();
        List<PostResult> homePosts = getPosts();
        genShinHomePosts.addAll(homePosts);
        log.info("获取社区帖子数: {}", genShinHomePosts.size());
        msg = msg + "获取社区帖子数: " + genShinHomePosts.size() + "\n";
        //执行任务
        Callable<Integer> viewPost = createTask(this, "viewPost", VIEW_NUM, genShinHomePosts);
        Callable<Integer> sharePost = createTask(this, "sharePost", SHARE_NUM, genShinHomePosts);
        Callable<Integer> upVotePost = createTask(this, "upVotePost", UP_VOTE_NUM, genShinHomePosts);

        FutureTask<Integer> vpf = new FutureTask<Integer>(viewPost);
        FutureTask<Integer> upf = new FutureTask<Integer>(upVotePost);
        FutureTask<Integer> spf = new FutureTask<Integer>(sharePost);

        List<FutureTask<Integer>> fts = Arrays.asList(vpf, upf, spf);
        for (FutureTask<Integer> ft : fts) {
            new Thread(ft).start();
        }
        countDownLatch.await();
        //打印日志
        log.info("浏览帖子: {},点赞帖子: {},分享帖子: {}", vpf.get(), upf.get(), spf.get());
        msg = msg + "浏览帖子: " + vpf.get() + ",点赞帖子: " + upf.get() + ",分享帖子: " + spf.get();
        map.put("flag", true);
        map.put("msg", msg);
        return map;
    }

    public Callable<Integer> createTask(Object obj, String methodName, int num, List<PostResult> posts) {
        return () -> {
            try {
                return doTask(obj, obj.getClass().getDeclaredMethod(methodName, PostResult.class), num, posts);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return 0;
        };
    }

    public int doTask(Object obj, Method method, int num, List<PostResult> posts) {
        countDownLatch.countDown();
        int sc = 0;
        // 保证每个浏览(点赞，分享)的帖子不重复
        HashSet<Object> set = new HashSet<>(num);
        for (int i = 0; i < num; i++) {
            int index = 0;
            while (set.contains(index)) {
                index = random.nextInt(posts.size());
            }
            set.add(index);
            try {
                method.invoke(obj, posts.get(index));
                sc++;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(random.nextInt(2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sc;
    }


    /**
     * 原神社区签到
     */
    public String sign() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("gids", hub.getForumId());

        Header[] headers = getHeaders(MihayouConstants.DS_TYPE_ONE);
        JSONObject signResult = HttpUtils.doPost(MiHoYoConfig.HUB_SIGN_URL, headers, data);

        if (signResult.getInteger("retcode") == 0 && !signResult.getString("message").contains("err")) {
            log.info("社区签到成功：{}", signResult.get("message"));
            return "社区签到成功：" + signResult.get("message");
        } else if (signResult.getInteger("retcode") == 1034) {
            log.warn("社区签到需要验证码：");
            Triple<Boolean, String, Header[]> getChallengeHeader = getChallengeHeader(headers);
            if (!getChallengeHeader.getLeft()) {
                log.error("社区签到失败: 验证码处理失败：{}", getChallengeHeader.getMiddle());
                return "社区签到失败: 验证码处理失败：" + getChallengeHeader.getMiddle();
            }
            Header[] challengeHeader = getChallengeHeader.getRight();
            signResult = HttpUtils.doPost(MiHoYoConfig.HUB_SIGN_URL, challengeHeader, data);
            log.info("验证码后社区签到请求返回：{}", signResult);
            if (signResult.getInteger("retcode") == 0) {
                log.info("社区签到成功：{}", signResult.get("message"));
                return "社区签到成功：" + signResult.get("message");
            } else {
                log.error("社区签到失败：社区签到验证码通过但签到失败通过: {}", signResult.get("message"));
                return "社区签到失败：社区签到验证码通过但签到失败通过：" + signResult.get("message");
            }
        } else {
            log.error("社区签到失败: {}", signResult.get("message"));
            return "社区签到失败：" + signResult.get("message");
        }
    }

    /**
     * 米游社获取验证码并通过
     * @param headers 原请求头
     * @return
     */
    private Triple<Boolean, String, Header[]> getChallengeHeader(Header[] headers) {
        JSONObject getChallengeResult = HttpUtils.doGet(MiHoYoConfig.BBS_GET_CAPTCHA_URL, headers, new HashMap<>());
        if (getChallengeResult.getInteger("retcode") != 0) {
            log.error("获取challenge失败：{}", getChallengeResult.get("message"));
            return ImmutableTriple.of(false, "获取取challenge失败：" + getChallengeResult.get("message"), null);
        } else {
            JSONObject challengeData = getChallengeResult.getJSONObject("data");
            String gt = challengeData.getString("gt");
            String challenge = challengeData.getString("challenge");
            Triple<Boolean, String, String> validateByRrOcr = CaptchaUtil.getValidateByRrOcr(gt, challenge, MiHoYoConfig.BBS_GET_CAPTCHA_URL);
            if (!validateByRrOcr.getLeft()) {
                log.error("获取validate失败：{}", validateByRrOcr.getMiddle());
                return ImmutableTriple.of(false, "获取validate失败：" + validateByRrOcr.getMiddle(), null);
            } else {
                String validate = validateByRrOcr.getRight();
                Map<String, Object> validateMap = new HashMap<>();
                validateMap.put("geetest_challenge", challenge);
                validateMap.put("geetest_seccode", validate + "|jordan");
                validateMap.put("geetest_validate", validate);
                JSONObject validateResult = HttpUtils.doPost(MiHoYoConfig.BBS_CAPTCHA_VERIFY_URL, headers, validateMap);
                if (getChallengeResult.getInteger("retcode") != 0) {
                    log.error("米游社校验validate失败：{}", validateResult.get("message"));
                    return ImmutableTriple.of(false, "米游社校验validate失败：" + validateResult.get("message"), null);
                }
                String successChallenge = validateResult.getJSONObject("data").getString("challenge");
                return ImmutableTriple.of(true, "ok",
                        new HeaderBuilder.Builder().addAll(headers).add("x-rpc-challenge", successChallenge).build());
            }
        }
    }

    /**
     * 原神频道
     *
     * @throws Exception
     */
    public List<PostResult> getGenShinHomePosts() throws Exception {
        return getPosts(String.format(MiHoYoConfig.HUB_LIST1_URL, hub.getForumId()));
    }

    /**
     * 旅行者社区讨论区
     *
     * @throws Exception
     */
    public List<PostResult> getPosts() throws Exception {
        return getPosts(String.format(MiHoYoConfig.HUB_LIST2_URL, hub.getId()));
    }


    /**
     * 获取帖子
     *
     * @throws Exception
     */
    public List<PostResult> getPosts(String url) throws Exception {
        JSONObject result = HttpUtils.doGet(url, getHeaders(MihayouConstants.DS_TYPE_TWO));
        if ("OK".equals(result.get("message"))) {
            JSONArray jsonArray = result.getJSONObject("data").getJSONArray("list");
            List<PostResult> posts = JSON.parseObject(JSON.toJSONString(jsonArray), new TypeReference<List<PostResult>>() {
            });
            return posts;
        } else {
            throw new Exception("帖子数为空，请查配置并更新！！！");
        }
    }


    /**
     * 看帖
     *
     * @param post
     */
    public boolean viewPost(PostResult post) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", post.getPost().getPost_id());
        data.put("is_cancel", false);
        Header[] headers = getHeaders(MihayouConstants.DS_TYPE_TWO);
        JSONObject result = HttpUtils.doGet(String.format(MiHoYoConfig.HUB_VIEW_URL, hub.getForumId()), headers, data);
        if (result.getInteger("retcode") == 0) {
            return true;
        } else if (result.getInteger("retcode") == 1034) {
            Triple<Boolean, String, Header[]> challengeHeader = getChallengeHeader(headers);
            if (!challengeHeader.getLeft()) {
                log.error("看贴失败：{}", challengeHeader.getMiddle());
                return false;
            } else {
                result = HttpUtils.doGet(String.format(MiHoYoConfig.HUB_VIEW_URL, hub.getForumId()), challengeHeader.getRight(), data);
                if (result.getInteger("retcode") == 0) {
                    return true;
                } else {
                    log.error("看贴失败：验证码后失败：{}", result.getString("message"));
                    return false;
                }
            }
        } else {
            log.error("看贴失败：{}", result.get("message"));
            return false;
        }
    }

    /**
     * 点赞
     *
     * @param post
     */
    public boolean upVotePost(PostResult post) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", post.getPost().getPost_id());
        data.put("is_cancel", false);
        Header[] headers = getHeaders(MihayouConstants.DS_TYPE_TWO);
        JSONObject result = HttpUtils.doPost(MiHoYoConfig.HUB_VOTE_URL, headers, data);
        if (result.getInteger("retcode") == 0) {
            return true;
        } else if (result.getInteger("retcode") == 1034) {
            Triple<Boolean, String, Header[]> challengeHeader = getChallengeHeader(headers);
            if (!challengeHeader.getLeft()) {
                log.error("点赞失败：{}", challengeHeader.getMiddle());
                return false;
            } else {
                result = HttpUtils.doPost(MiHoYoConfig.HUB_VOTE_URL, challengeHeader.getRight(), data);
                if (result.getInteger("retcode") == 0) {
                    return true;
                } else {
                    log.error("点赞失败：验证码后失败：{}", result.getString("message"));
                    return false;
                }
            }
        } else {
            log.error("点赞失败：{}", result.get("message"));
            return false;
        }
    }

    /**
     * 分享
     *
     * @param post
     */
    public boolean sharePost(PostResult post) {
        Header[] headers = getHeaders(MihayouConstants.DS_TYPE_TWO);
        JSONObject result = HttpUtils.doGet(String.format(MiHoYoConfig.HUB_SHARE_URL, post.getPost().getPost_id()), headers);
        if (result.getInteger("retcode") == 0) {
            return true;
        } else if (result.getInteger("retcode") == 1034) {
            Triple<Boolean, String, Header[]> challengeHeader = getChallengeHeader(headers);
            if (!challengeHeader.getLeft()) {
                log.error("分享失败：{}", challengeHeader.getMiddle());
                return false;
            } else {
                result = HttpUtils.doGet(String.format(MiHoYoConfig.HUB_SHARE_URL, post.getPost().getPost_id()), challengeHeader.getRight());
                if (result.getInteger("retcode") == 0) {
                    return true;
                } else {
                    log.error("分享失败：验证码后失败：{}", result.getString("message"));
                    return false;
                }
            }
        } else {
            log.error("分享失败：{}", result.get("message"));
            return false;
        }
    }


    /**
     * 获取 stoken
     *
     * @throws URISyntaxException
     */
    public String getCookieToken() throws Exception {
        JSONObject result = HttpUtils.
                doGet(String.format(MiHoYoConfig.HUB_COOKIE2_URL, getCookieByName("login_ticket"), getCookieByName("account_id")), getHeaders(MihayouConstants.DS_TYPE_TWO));
        if (!"OK".equals(result.get("message"))) {
            log.info("login_ticket已失效,请重新登录获取");
            throw new Exception("login_ticket已失效,请重新登录获取");
        }
        return (String) result.getJSONObject("data").getJSONArray("list").getJSONObject(0).get("token");
    }

    public String getCookieByName(String name) {
        String[] split = cookie.split(";");
        for (String s : split) {
            String h = s.trim();
            if (h.startsWith(name)) {
                return h.substring(h.indexOf('=') + 1);
            }
        }
        return null;
    }

    @Override
    public Header[] getHeaders(String dsType) {
        HeaderBuilder.Builder builder = new HeaderBuilder.Builder()
                .add("x-rpc-client_type", getClientType())
                .add("x-rpc-app_version", getAppVersion())
                .add("x-rpc-sys_version", "12").add("x-rpc-channel", "miyousheluodi")
                .add("x-rpc-device_id", UUID.randomUUID().toString().replace("-", "").toLowerCase())
                .add("x-rpc-device_name", "Xiaomi Redmi Note 4")
                .add("Referer", "https://app.mihoyo.com")
                .add("Content-Type", "application/json")
                .add("Host", "bbs-api.mihoyo.com")
//        .add("Content-Length", "41");
                .add("Connection", "Keep-Alive")
                .add("Accept-Encoding", "gzip")
                .add("User-Agent", "okhttp/4.8.0")
                .add("x-rpc-device_model", "Redmi Note 4")
                .add("isLogin", "true")

                .add("cookie", "stuid=" + stuid + ";stoken=" + stoken + ";");

        if (MihayouConstants.DS_TYPE_ONE.equals(dsType)) {
            JSONObject json = new JSONObject();
            json.put("gids", hub.getForumId());

            builder.add("DS", getDS(json.toString()));
        } else if (MihayouConstants.DS_TYPE_TWO.equals(dsType)){
            builder.add("DS", getDS(false));
        }

        return builder.build();
    }

    public String getDS(boolean isWeb) {
        String i = (System.currentTimeMillis() / 1000) + "";
        String r = getRandomStr();
        String salt;
        if (isWeb) {
            salt = MihayouConstants.COMMUNITY_SIGN_SALT;
        } else {
            salt = MihayouConstants.MIHOYO_BBS_SALT;
        }
        return createDS(salt, i, r);
    }

    private String createDS(String n, String i, String r) {
        String c = DigestUtils.md5Hex("salt=" + n + "&t=" + i + "&r=" + r);
        return String.format("%s,%s,%s", i, r, c);
    }
}