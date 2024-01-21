package xyz.qy.imcommon.contant;

public class IMRedisKey {
    // im-server最大id,从0开始递增
    public final static String  IM_MAX_SERVER_ID = "im:max_server_id";
    // 用户ID所连接的IM-server的ID
    public final static String  IM_USER_SERVER_ID = "im:user:server_id";
    // 未读私聊消息队列
    public final static String IM_MESSAGE_PRIVATE_QUEUE = "im:message:private";
    // 未读群聊消息队列
    public final static String IM_MESSAGE_GROUP_QUEUE = "im:message:group";
    // 私聊消息发送结果队列
    public final static String IM_RESULT_PRIVATE_QUEUE = "im:result:private";
    // 群聊消息发送结果队列
    public final static String IM_RESULT_GROUP_QUEUE = "im:result:group";
    // 验证码 redis key
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";
}
