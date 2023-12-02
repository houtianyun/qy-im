package xyz.qy.implatform.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * @description: 说说
 * @author: HouTianYun
 * @create: 2023-11-20 20:49
 **/
@Data
public class TalkVO {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @NotBlank(message = "说说内容为空")
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
    private String deleted;

    @ApiModelProperty(value = "创建者")
    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField("update_by")
    @ApiModelProperty(value = "更新者")
    private Long updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
