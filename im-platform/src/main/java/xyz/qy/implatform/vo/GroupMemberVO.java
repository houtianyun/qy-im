package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("群成员信息VO")
public class GroupMemberVO {
    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("群内显示名称")
    private String aliasName;

    @ApiModelProperty("头像")
    private String headImage;

    @ApiModelProperty("是否已退出")
    private Boolean quit;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("群聊id")
    private String groupId;

    @ApiModelProperty("模板群聊id")
    private Long templateGroupId;

    @ApiModelProperty("模板人物id")
    private Long templateCharacterId;

    @ApiModelProperty("模板人物名称")
    private String templateCharacterName;

    @ApiModelProperty("模板人物头像")
    private String templateCharacterAvatar;

    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("模板人物头像id")
    private Long characterAvatarId;

    @ApiModelProperty("头像别名")
    private String avatarAlias;

    @ApiModelProperty("用户头像")
    private String userAvatar;

    @ApiModelProperty("是否展示群成员昵称")
    private Boolean showNickName = false;
}
