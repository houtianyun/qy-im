package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 动态评论
 * @author: HouTianYun
 * @create: 2023-12-25 20:24
 **/
@Data
public class TalkCommentVO {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "动态id")
    private Long talkId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "评论内容")
    private String content;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "回复的评论id")
    private Long replyCommentId;

    @ApiModelProperty(value = "回复用户id")
    private Long replyUserId;

    @ApiModelProperty(value = "回复用户头像")
    private String replyUserAvatar;

    @ApiModelProperty(value = "回复用户昵称")
    private String replyUserNickname;

    @ApiModelProperty(value = "是否匿名")
    private Boolean anonymous = Boolean.FALSE;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "IP来源")
    private String ipAddress;
}
