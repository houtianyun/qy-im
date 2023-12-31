package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @description: 动态
 * @author: HouTianYun
 * @create: 2023-11-20 20:49
 **/
@Data
public class TalkVO {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "已被选择的角色id")
    private Set<Long> selectedCharacterIds;

    @ApiModelProperty(value = "用户评论角色id")
    private Long userCommentCharacterId;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "是否匿名")
    private Boolean anonymous;

    @NotBlank(message = "动态内容为空")
    @ApiModelProperty(value = "内容")
    private String content;

    @Size(max = 30, message = "最多只能上传20张图片")
    @ApiModelProperty(value = "图片URL")
    private List<String> imgUrls;

    @ApiModelProperty(value = "视频URL")
    private String videoUrl;

    @NotNull(message = "未选择公布范围")
    @ApiModelProperty(value = "公布范围")
    private Integer scope;

    @ApiModelProperty(value = "发布地址")
    private String address;

    @ApiModelProperty(value = "是否删除")
    private Boolean deleted;

    @ApiModelProperty(value = "是否点赞")
    private Boolean isLike = Boolean.FALSE;

    @ApiModelProperty(value = "是否自己的")
    private Boolean isOwner = Boolean.FALSE;

    @ApiModelProperty(value = "能否选择角色")
    private Boolean enableCharacterChoose = Boolean.FALSE;

    @ApiModelProperty(value = "动态赞星")
    private List<TalkStarVO> talkStarVOS;

    @ApiModelProperty(value = "动态评论")
    private List<TalkCommentVO> talkCommentVOS;

    @ApiModelProperty(value = "创建者")
    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新者")
    private Long updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
