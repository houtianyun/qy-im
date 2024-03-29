package xyz.qy.implatform.contant;

public final class Constant {
    public Constant() {
    }

    // 最大图片上传大小
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    // 最大上传文件大小
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    // 群聊最大人数
    public static final long MAX_GROUP_MEMBER = 500;
    // 常量1：是
    public static final int YES = 1;
    // 常量0：否
    public static final int NO = 0;
    // 常量1：是
    public static final String YES_STR = "1";
    // 常量0：否
    public static final String NO_STR = "0";
    // 常量1：已删除
    public static final int DELETED = 1;
    // 常量0：未删除
    public static final int NO_DELETED = 0;
    // 模板群聊切换时间间隔1800s
    public static final int SWITCH_INTERVAL = 1800;
    // 媒体播放字典key
    public static final String MEDIA_SWITCH = "sys_media_switch";
    // 公共群聊id
    public static final long COMMON_GROUP_ID = 1L;
    // 管理员用户id
    public static final long ADMIN_USER_ID = 1L;
    // 管理员欢迎语
    public static final String ADMIN_WELCOME_MSG = "感谢您的到来#玫瑰;";
    // 用户可以创建的最多模板群聊数量
    public static final int USER_MAX_TEMPLATE_GROUP_NUM = 10;
    // 用户创建的模板群聊的最多模板人物数量
    public static final int USER_MAX_TEMPLATE_CHARACTER_NUM = 100;
    // 每位模板人物最多配置的人物头像数量
    public static final int MAX_CHARACTER_AVATAR_NUM = 20;
    // 当前页
    public static final String PAGE_NO = "pageNo";
    // 分页大小
    public static final String PAGE_SIZE = "pageSize";
    // 默认分页大小
    public static final String DEFAULT_SIZE = "10";
    // 默认第一页
    public static final String DEFAULT_PAGE_NO = "1";
    // 英文逗号（,）
    public static final String COMMA = ",";
    // 匿名用户id
    public static final Long ANONYMOUS_USER_ID = -1l;
    // 匿名用户名称
    public static final String ANONYMOUS_NICK_NAME = "匿名用户";
}
