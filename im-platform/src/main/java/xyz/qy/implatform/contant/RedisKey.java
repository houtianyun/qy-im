package xyz.qy.implatform.contant;

public class RedisKey {
    // 已读群聊消息位置(已读最大id)
    public final static String IM_GROUP_READED_POSITION = "im:readed:group:position:";
    // 缓存前缀
    public final static String  IM_CACHE = "im:cache:";
    // 缓存是否好友：bool
    public final static String  IM_CACHE_FRIEND = IM_CACHE+"friend";
    // 缓存群聊信息
    public final static String  IM_CACHE_GROUP = IM_CACHE+"group";
    // 缓存群聊成员id
    public final static String IM_CACHE_GROUP_MEMBER_ID = IM_CACHE+"group_member_ids";
    // 验证码 redis key
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";
    // 验证码有效期（分钟）
    public static final Integer CAPTCHA_EXPIRATION = 2;
    // 访客
    public static final String UNIQUE_VISITOR = "im_unique_visitor";
    // 登录页媒体信息缓存key
    public static final String LOGIN_MEDIA = IM_CACHE + "media";
    // 是否开启媒体播放
    public static final String ON_OFF_DISPLAY_MEDIA = IM_CACHE + "display_media";
    // webrtc 会话信息
    public final static String IM_WEBRTC_SESSION = "im:webrtc:session";
}
