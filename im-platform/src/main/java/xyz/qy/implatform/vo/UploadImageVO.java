package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("图片上传VO")
public class UploadImageVO {
    @ApiModelProperty(value = "原图")
    private String originUrl;

    @ApiModelProperty(value = "缩略图")
    private String thumbUrl;

    @ApiModelProperty(value = "文件原名称，无格式后缀")
    private String name;

    @ApiModelProperty(value = "文件原名称")
    private String originName;
}
