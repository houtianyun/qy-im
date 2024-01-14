create table im_character_avatar
(
    id                      bigint auto_increment comment '主键'
        primary key,
    template_character_id   bigint                               null comment '模板人物id',
    template_character_name varchar(50)                          not null comment '模板人物名称',
    name                    varchar(50)                          not null comment '头像名称',
    avatar                  varchar(1000)                        not null comment '头像链接',
    level                   tinyint  default 0                   not null comment '头像等级',
    status                  varchar(2)                           null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    create_time             datetime default current_timestamp() not null comment '创建时间',
    update_time             datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    create_by               bigint                               null comment '创建人',
    update_by               bigint                               null comment '更新人',
    deleted                 tinyint  default 0                   not null comment '是否删除：0-否；1-是'
)
    comment '模板人物头像配置表';


create table im_friend
(
    id                bigint auto_increment comment 'id'
        primary key,
    user_id           bigint                                   not null comment '用户id',
    friend_id         bigint                                   not null comment '好友id',
    friend_nick_name  varchar(255)                             not null comment '好友昵称',
    friend_head_image varchar(255) default ''                  null comment '好友头像',
    created_time      datetime     default current_timestamp() null comment '创建时间'
)
    comment '好友表';

create index idx_friend_id on im_friend (friend_id);

create index idx_user_id on im_friend (user_id);


create table im_group
(
    id                bigint auto_increment comment 'id'
        primary key,
    name              varchar(255)                              not null comment '群名字',
    owner_id          bigint                                    not null comment '群主id',
    head_image        varchar(255)  default ''                  null comment '群头像',
    head_image_thumb  varchar(255)  default ''                  null comment '群头像缩略图',
    is_template       tinyint(1)    default 0                   not null comment '是否模板群聊（0：否；1：是）',
    template_group_id bigint                                    null comment '模板群聊id',
    notice            varchar(1024) default ''                  null comment '群公告',
    remark            varchar(255)  default ''                  null comment '群备注',
    deleted           tinyint(1)    default 0                   not null comment '是否已删除',
    created_time      datetime      default current_timestamp() null comment '创建时间',
    switch_time       datetime                                  null comment '切换模板群聊时间'
)
    comment '群';


create table im_group_member
(
    id                    bigint auto_increment comment 'id'
        primary key,
    group_id              bigint                                   not null comment '群id',
    user_id               bigint                                   not null comment '用户id',
    alias_name            varchar(255) default ''                  null comment '组内显示名称',
    head_image            varchar(255) default ''                  null comment '用户头像',
    is_template           tinyint(1)   default 0                   not null comment '是否模板人物',
    template_character_id bigint       default 0                   not null comment '模板人物id',
    character_avatar_id   bigint                                   null comment '模板人物头像id',
    avatar_alias          varchar(50)                              null comment '头像别名',
    remark                varchar(255) default ''                  null comment '备注',
    quit                  tinyint(1)   default 0                   not null comment '是否已退出',
    show_nickname         tinyint(1)   default 0                   not null comment '是否展示群成员昵称（0：否；1：是）',
    created_time          datetime     default current_timestamp() null comment '创建时间',
    switch_time           datetime                                 null comment '模板人物切换时间',
    constraint unique_idx_1
        unique (group_id, user_id)
)
    comment '群成员表';

create index idx_group_id on im_group_member (group_id);

create index idx_user_id on im_group_member (user_id);


create table im_group_message
(
    id        bigint auto_increment comment 'id'
        primary key,
    group_id  bigint                                 not null comment '群id',
    send_id   bigint                                 not null comment '发送用户id',
    content   text                                   null comment '发送内容',
    type      tinyint(1)                             not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 10:系统提示',
    status    tinyint(1) default 0                   null comment '状态 0:正常  2:撤回',
    send_time datetime   default current_timestamp() null comment '发送时间'
)
    comment '群消息表';

create index idx_group_id on im_group_message (group_id);


create table im_media_material
(
    id               int auto_increment comment '主键'
        primary key,
    title            varchar(100)                         not null comment '素材名称',
    description      varchar(200)                         null comment '素材简介',
    url              varchar(1000)                        not null comment '素材链接',
    cover_image      varchar(1000)                        null comment '封面图片',
    type             varchar(10)                          not null comment '素材类型',
    format           varchar(10)                          not null comment '格式',
    display_duration int                                  not null comment '素材展示时长（单位秒）',
    sort             int                                  not null comment '排序号',
    status           tinyint  default 1                   not null comment '状态（1：启用；0：停用）',
    start_time       datetime                             null comment '素材展示开始时间',
    end_time         datetime                             null comment '素材展示结束时间',
    create_time      datetime default current_timestamp() not null comment '创建时间',
    update_time      datetime default current_timestamp() not null on update current_timestamp() comment '更新时间'
)
    comment '媒体素材表';

create index idx1 on im_media_material (sort);


create table im_picture
(
    id           int auto_increment comment '主键'
        primary key,
    picture_name varchar(100)                           not null comment '图片名称',
    category     varchar(20)                            not null comment '图片类型',
    format       varchar(20)                            not null comment '格式',
    url          varchar(1000)                          null comment '图片链接',
    origin       varchar(100)                           not null comment '来源',
    origin_id    varchar(50)                            null comment '图片来源id',
    status       tinyint(1) default 1                   not null comment '状态（1：有效；0：失效）',
    create_time  datetime   default current_timestamp() null comment '创建时间',
    update_time  datetime   default current_timestamp() null on update current_timestamp() comment '更新时间',
    constraint idx1
        unique (origin_id)
)
    comment '图片表';


create table im_private_message
(
    id        bigint auto_increment comment 'id'
        primary key,
    send_id   bigint                               not null comment '发送用户id',
    recv_id   bigint                               not null comment '接收用户id',
    content   text                                 null comment '发送内容',
    type      tinyint(1)                           not null comment '消息类型 0:文字 1:图片 2:文件 3:语音 10:系统提示',
    status    tinyint(1)                           not null comment '状态 0:未读 1:已读 2:撤回',
    send_time datetime default current_timestamp() null comment '发送时间'
)
    comment '私聊消息';

create index idx_send_recv_id on im_private_message (send_id, recv_id);


create table im_template_character
(
    id                bigint auto_increment comment '主键'
        primary key,
    template_group_id bigint                                 not null comment '模板群聊id',
    name              varchar(50)                            not null comment '人物名称',
    description       varchar(200)                           null comment '人物描述',
    avatar            varchar(500)                           not null comment '人物头像',
    create_time       datetime   default current_timestamp() not null comment '创建时间',
    update_time       datetime   default current_timestamp() not null on update current_timestamp() comment '更新时间',
    create_by         bigint                                 null comment '创建人',
    update_by         bigint                                 null comment '更新人',
    status            varchar(2)                             null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    deleted           tinyint(1) default 0                   not null comment '是否删除：0-否；1-是'
)
    comment '模板群的模板人物';


create table im_template_group
(
    id          bigint auto_increment comment '主键'
        primary key,
    group_name  varchar(20)                            not null comment '群名称',
    description varchar(200)                           null comment '描述',
    avatar      varchar(1000)                          null comment '群头像',
    count       int        default 0                   not null comment '群人数',
    status      varchar(2)                             null comment '状态：0-待审批；1-审核中；2-已发布；3-未通过',
    create_time datetime   default current_timestamp() not null comment '创建时间',
    update_time datetime   default current_timestamp() not null on update current_timestamp() comment '更新时间',
    create_by   bigint                                 null comment '创建人',
    update_by   bigint                                 null comment '更新人',
    deleted     tinyint(1) default 0                   not null comment '是否删除：0-否；1-是'
)
    comment '模板群';


create table im_user
(
    id               bigint auto_increment comment 'id'
        primary key,
    user_name        varchar(255)                              not null comment '用户名',
    nick_name        varchar(255)                              not null comment '用户昵称',
    head_image       varchar(255)  default ''                  null comment '用户头像',
    head_image_thumb varchar(255)  default ''                  null comment '用户头像缩略图',
    password         varchar(255)                              not null comment '密码(明文)',
    sex              tinyint(1)    default 0                   null comment '性别 0:男 1::女',
    type             smallint(1)   default 1                   null comment '用户类型 1:普通用户 2:审核账户',
    signature        varchar(1024) default ''                  null comment '个性签名',
    qq_open_id       varchar(255)                              null comment 'QQ开放id',
    qq_access_token  varchar(255)                              null comment 'QQ访问令牌',
    login_type       tinyint(1)                                null comment '登录类型',
    is_disable       tinyint(1)    default 0                   not null comment '是否禁用',
    ip_address       varchar(50)                               null comment '用户登录IP',
    ip_source        varchar(50)                               null comment 'IP来源',
    province         varchar(50)                               null comment '省份',
    city             varchar(50)                               null comment '城市',
    last_login_time  datetime      default current_timestamp() null on update current_timestamp() comment '最后登录时间',
    create_time      datetime      default current_timestamp() null comment '创建时间',
    update_time      datetime      default current_timestamp() null on update current_timestamp() comment '更新时间',
    constraint idx_user_name
        unique (user_name)
)
    comment '用户表';

create index idx_nick_name on im_user (nick_name);


create table im_visitor
(
    id               int auto_increment comment '主键'
        primary key,
    ip               varchar(50)                          null comment 'IP地址',
    nation           varchar(50)                          null comment '国家',
    pro              varchar(50)                          null comment '省',
    city             varchar(50)                          null comment '市',
    browser          varchar(100)                         null comment '浏览器',
    operating_system varchar(100)                         null comment '操作系统',
    addr             varchar(200)                         null comment '地址',
    location_info    varchar(1000)                        null comment '位置信息json结果',
    create_time      datetime default current_timestamp() not null comment '创建时间'
)
    comment '访客信息表';


create table im_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int(4)       default 0   null comment '字典排序',
    dict_label  varchar(100) default ''  null comment '字典标签',
    dict_value  varchar(100) default ''  null comment '字典键值',
    dict_type   varchar(100) default ''  null comment '字典类型',
    css_class   varchar(100)             null comment '样式属性（其他样式扩展）',
    list_class  varchar(100)             null comment '表格回显样式',
    is_default  char         default 'N' null comment '是否默认（Y是 N否）',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '字典数据表';

create table im_music
(
    id          int auto_increment
        primary key,
    origin_id   varchar(100)                           not null comment '来源id',
    singer      varchar(100)                           null comment '歌手',
    music_name  varchar(100)                           null comment '歌曲名称',
    music_url   varchar(255)                           null comment '歌曲链接',
    has_crawl   tinyint(1) default 0                   not null comment '是否已爬取',
    create_time datetime   default current_timestamp() null,
    update_time datetime   default current_timestamp() null on update current_timestamp(),
    constraint idx1
        unique (origin_id)
)
    comment '音乐表';


create table im_group_msg_read_position
(
    group_id     bigint                               not null comment '群聊id',
    user_id      bigint                               not null comment '用户id',
    group_msg_id bigint                               not null comment '群聊消息id',
    create_time  datetime default current_timestamp() null comment '创建时间',
    update_time  datetime default current_timestamp() null on update current_timestamp() comment '更新时间',
    constraint idx1
        unique (group_id, user_id)
)
    comment '记录用户群聊消息的已读位置表';


create table im_talk
(
    id           int auto_increment comment '主键'
        primary key,
    user_id      bigint               not null comment '用户id',
    character_id bigint               null comment '角色人物id',
    nick_name    varchar(50)          null comment '用户昵称',
    avatar       varchar(1000)        null comment '用户头像',
    content      varchar(500)         not null comment '内容',
    img_url      varchar(2000)        null comment '图片（多个使用,隔开）',
    video_url    varchar(500)         null comment '视频连接',
    scope        tinyint(1)           not null comment '公布范围（1：私密；2：好友可见；3：群友可见；4：公开）',
    address      varchar(200)         null comment '发布地址',
    anonymous    tinyint(1) default 0 not null comment '是否匿名：0-否；1-是',
    create_time  datetime             not null comment '创建时间',
    create_by    bigint               not null comment '创建人',
    update_time  datetime             null comment '更新时间',
    update_by    bigint               null comment '更新人',
    deleted      tinyint(1) default 0 not null comment '是否删除（1：是；0：否）'
)
    comment '动态表';


create table im_talk_star
(
    id           bigint               not null comment '主键'
        primary key,
    talk_id      bigint               not null comment '动态id',
    user_id      bigint               not null comment '用户id',
    nickname     varchar(50)          null comment '用户昵称',
    avatar       varchar(1000)        null comment '用户头像',
    character_id bigint               null comment '角色id',
    anonymous    tinyint(1) default 0 not null comment '是否匿名：0-否；1-是',
    create_time  datetime             not null comment '创建时间',
    create_by    bigint               not null comment '创建人',
    update_time  datetime             null comment '更新时间',
    update_by    bigint               null comment '更新人',
    deleted      tinyint(1) default 0 not null comment '是否删除（0：否；1：是）',
    constraint idx1
        unique (talk_id, user_id)
)
    comment '动态赞星表';


create table im_talk_comment
(
    id                  bigint               not null comment '主键'
        primary key,
    talk_id             bigint               not null comment '动态id',
    user_id             bigint               not null comment '用户id',
    user_nickname       varchar(50)          not null comment '用户昵称',
    user_avatar         varchar(1000)        null comment '用户头像',
    content             varchar(1000)        not null comment '评论内容',
    character_id        bigint               null comment '角色id',
    reply_comment_id    bigint               null comment '回复的评论id',
    reply_user_id       bigint               null comment '回复用户id',
    reply_user_avatar   varchar(1000)        null comment '回复用户头像',
    reply_user_nickname varchar(50)          null comment '回复用户昵称',
    anonymous           tinyint(1) default 0 not null comment '是否匿名：0-否；1-是',
    ip                  varchar(200)         null comment 'IP地址',
    ip_address          varchar(200)         null comment 'IP来源',
    deleted             tinyint(1) default 0 not null comment '是否删除（0：否；1：是）',
    create_time         datetime             not null comment '创建时间',
    create_by           bigint               not null comment '创建人',
    update_time         datetime             null comment '更新时间',
    update_by           bigint               null comment '更新人'
)
    comment '动态评论表';
