package xyz.qy.implatform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import xyz.qy.imcommon.serializer.DateToLongSerializer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("群聊消息VO")
public class GroupMessageVO {
    @ApiModelProperty(value = "消息id")
    private Long id;

    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @ApiModelProperty(value = " 发送者id")
    private Long sendId;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息内容类型 具体枚举值由应用层定义")
    private Integer type;

    @ApiModelProperty(value = " 状态")
    private Integer status;

    @ApiModelProperty(value = "发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
