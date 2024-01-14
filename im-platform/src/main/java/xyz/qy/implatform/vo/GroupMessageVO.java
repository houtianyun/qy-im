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
    /**
     * 消息id
     */
    private Long id;

    /**
     * 群聊id
     */
    private Long groupId;

    /**
     * 发送者id
     */
    private Long sendId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息内容类型 具体枚举值由应用层定义
     */
    private Integer type;

    /**
     * 发送时间
     */
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
