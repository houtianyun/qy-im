package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 动态赞星
 * @author: HouTianYun
 * @create: 2023-12-25 20:19
 **/
@Data
public class TalkStarVO {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "动态id")
    private Long talkId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "是否匿名")
    private Boolean anonymous = Boolean.FALSE;
}
